package com.coffee4j.controller;

import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * The REST controller used to interact with the Coffee4j user data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 23, 2022
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
     * Attempts to create a new user using the specified username and password. A username and password are required
     * for creation.
     *
     * @param username the username to be used in the operation
     * @param password the password to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Map<String, ?>> create(@RequestParam String username, @RequestParam String password) {
        if (username.length() > MAX_USERNAME_LENGTH) {
            String message = "A username cannot exceed %d characters".formatted(MAX_USERNAME_LENGTH);

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", message
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Table<Record> usersTable = DSL.table("users");

        Field<String> usernameField = DSL.field("username", String.class);

        Field<String> passwordHashField = DSL.field("password_hash", String.class);

        String salt = BCrypt.gensalt();

        String passwordHash = BCrypt.hashpw(password, salt);

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.insertInto(usersTable)
                                 .columns(usernameField, passwordHashField)
                                 .values(username, passwordHash)
                                 .execute();
        } catch (SQLException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A user with the specified username and password could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end try catch

        if (rowsChanged == 0) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A user with the specified username and password could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String locationString = "http://localhost:8080/api/users";

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        Map<String, ?> successMap = Map.of(
            "success", true,
            "message", "A user with the specified username and password was successfully created"
        );

        return new ResponseEntity<>(successMap, httpHeaders, HttpStatus.CREATED);
    } //create

    /**
     * Attempts to read the user data of the current logged-in user. Assuming data exists, the ID and username of the
     * current logged-in user are returned.
     *
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Map<String, ?>> read() {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Field<Integer> idField = DSL.field("id", Integer.class);

        Field<String> usernameField = DSL.field("username", String.class);

        Table<Record> usersTable = DSL.table("users");

        int id = user.id();

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.select(idField, usernameField)
                            .from(usersTable)
                            .where(idField.eq(id))
                            .fetchOne();
        } catch (SQLException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user's data could not be retrieved"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (record == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user's data could not be retrieved"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        int recordId = record.getValue(idField);

        String recordUsername = record.getValue(usernameField);

        Map<String, ?> userData = Map.of(
            "id", recordId,
            "username", recordUsername
        );

        Map<String, ?> successMap = Map.of(
            "success", true,
            "user", userData
        );

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the user data of the current logged-in user using the specified username and password. A
     * user's username and password can be updated. At least one update is required.
     *
     * @param username the username to be used in the operation
     * @param password the password to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Map<String, ?>> update(@RequestParam(required = false) String username,
                                                 @RequestParam(required = false) String password) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        return new ResponseEntity<>(HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the user data of the current logged-in user.
     *
     * @return a {@link ResponseEntity} containing the outcome of the delete operation
     */
    @DeleteMapping
    public ResponseEntity<Map<String, ?>> delete() {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        return new ResponseEntity<>(HttpStatus.OK);
    } //delete
}