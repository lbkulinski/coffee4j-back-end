package com.coffee4j.controller;

import static com.coffee4j.generated.tables.Users.USERS;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;

import java.sql.*;
import java.util.*;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import com.coffee4j.Utilities;

import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.coffee4j.security.User;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * The REST controller used to interact with the Coffee4j user data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 22, 2022
 */
@RestController
@RequestMapping("api/users")
public final class UserController {
    /**
     * The maximum username length to be used in the {@link UserController} class.
     */
    private static final int MAX_USERNAME_LENGTH;

    /**
     * The {@link Logger} to be used in the {@link UserController} class.
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

        String salt = BCrypt.gensalt();

        String passwordHash = BCrypt.hashpw(password, salt);

        byte[] passwordHashBytes = passwordHash.getBytes();

        Integer id = null;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            Record record = context.insertInto(USERS, USERS.USERNAME, USERS.PASSWORD_HASH)
                                   .values(username, passwordHashBytes)
                                   .returningResult(USERS.ID)
                                   .fetchOne();

            if (record != null) {
                id = record.getValue(USERS.ID);
            } //end if
        } catch (SQLException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (id == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Map<String, ?> successMap = Map.of(
            "success", true,
            "message", "The user was successfully created"
        );

        String locationString = "http://localhost:8080/api/users?id=%d".formatted(id);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

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

        int id = user.id();

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            result = context.select(USERS.ID, USERS.USERNAME)
                            .from(USERS)
                            .where(USERS.ID.eq(id))
                            .fetch();
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

        Set<Map<String, ?>> userData = new HashSet<>();

        for (Record record : result) {
            int recordId = record.getValue(USERS.ID);

            String recordUsername = record.getValue(USERS.USERNAME);

            Map<String, ?> userDatum = Map.of(
                "id", recordId,
                "username", recordUsername
            );

            userData.add(userDatum);
        } //end for

        Map<String, ?> responseMap;

        if (userData.isEmpty()) {
            responseMap = Map.of(
                "success", false,
                "message", "The user's data could not be retrieved"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "users", userData
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
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