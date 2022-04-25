package com.coffee4j.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import com.coffee4j.Body;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.jooq.Table;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.Field;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.DriverManager;
import com.coffee4j.Utilities;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import java.sql.SQLException;
import org.jooq.exception.DataAccessException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import com.coffee4j.security.User;
import java.util.Map;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.HashMap;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * The REST controller used to interact with the Coffee4j user data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 24, 2022
 */
@RestController
@RequestMapping("api/users")
public final class UserController {
    /**
     * The maximum username length of the {@link UserController} class.
     */
    private static final int MAX_USERNAME_LENGTH;

    /**
     * The {@link Logger} of the {@link UserController} class.
     */
    private static final Logger LOGGER;

    static {
        MAX_USERNAME_LENGTH = 15;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to create a new user. A username and password are required for creation.
     *
     * @param username the username to be used in the operation
     * @param password the password to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam String username, @RequestParam String password) {
        if (username.length() > MAX_USERNAME_LENGTH) {
            String content = "A username cannot exceed %d characters".formatted(MAX_USERNAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Table<Record> usersTable = DSL.table("`users`");

        Field<String> usernameField = DSL.field("`username`", String.class);

        Field<String> passwordHashField = DSL.field("`password_hash`", String.class);

        String salt = BCrypt.gensalt();

        String passwordHash = BCrypt.hashpw(password, salt);

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.insertInto(usersTable)
                                 .columns(usernameField, passwordHashField)
                                 .values(username, passwordHash)
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A user with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A user with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String locationString = "http://localhost:8080/api/users";

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        String content = "A user with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.CREATED);
    } //create

    /**
     * Attempts to read the user data of the current logged-in user. Assuming data exists, the ID and username of the
     * current logged-in user are returned.
     *
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read() {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        Field<String> usernameField = DSL.field("`username`", String.class);

        Table<Record> usersTable = DSL.table("`users`");

        int id = user.id();

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.select(idField, usernameField)
                            .from(usersTable)
                            .where(idField.eq(id))
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The user's data could not be retrieved";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (record == null) {
            String content = "The user's data could not be retrieved";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int recordId = record.getValue(idField);

        String recordUsername = record.getValue(usernameField);

        Map<String, ?> content = Map.of(
            "id", recordId,
            "username", recordUsername
        );

        Body<Map<String, ?>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the user data of the current logged-in user. A user's username and password can be updated.
     * At least one update is required.
     *
     * @param username the username to be used in the operation
     * @param password the password to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam(required = false) String username,
                                          @RequestParam(required = false) String password) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Map<Field<String>, String> fieldToNewValue = new HashMap<>();

        if (username != null) {
            Field<String> usernameField = DSL.field("`username`", String.class);

            fieldToNewValue.put(usernameField, username);
        } //end if

        if (password != null) {
            Field<String> passwordHashField = DSL.field("`password_hash`", String.class);

            String salt = BCrypt.gensalt();

            String passwordHash = BCrypt.hashpw(password, salt);

            fieldToNewValue.put(passwordHashField, passwordHash);
        } //end if

        if (fieldToNewValue.isEmpty()) {
            String content = "At lease one update is required";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Table<Record> usersTable = DSL.table("`users`");

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        int id = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.update(usersTable)
                                 .set(fieldToNewValue)
                                 .where(idField.eq(id))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The user's data could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "The user's data could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "The user's data was successfully updated";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the user data of the current logged-in user.
     *
     * @return a {@link ResponseEntity} containing the outcome of the delete operation
     */
    @DeleteMapping
    public ResponseEntity<Body<?>> delete() {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Table<Record> usersTable = DSL.table("`users`");

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        int id = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.delete(usersTable)
                                 .where(idField.eq(id))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The user's data could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "The user's data could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "The user's data was successfully deleted";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //delete
}