package com.coffee4j.controller;

import com.coffee4j.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.sql.*;
import java.util.*;

/**
 * The REST controller used to interact with the Coffee4j user data.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 28, 2022
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
     * Attempts to create a new user using the specified parameters. A username and password are required for creation.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Map<String, ?>> create(@RequestBody Map<String, Object> parameters) {
        String usernameKey = "username";

        String username = Utilities.getParameter(parameters, usernameKey, String.class);

        if (username == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A username is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } else if (username.length() > MAX_USERNAME_LENGTH) {
            String message = "A username cannot exceed %d characters".formatted(MAX_USERNAME_LENGTH);

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", message
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String passwordKey = "password";

        String password = Utilities.getParameter(parameters, passwordKey, String.class);

        if (password == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A password is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String salt = BCrypt.gensalt();

        String passwordHash = BCrypt.hashpw(password, salt);

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String insertUserStatement = """
            INSERT INTO `users` (
                `username`,
                `password_hash`
            ) VALUES (
                ?,
                ?
            )""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(insertUserStatement);

            preparedStatement.setString(1, username);

            preparedStatement.setString(2, passwordHash);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            UserController.LOGGER.atError()
                                 .withThrowable(e)
                                 .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                UserController.LOGGER.atError()
                                     .withThrowable(e)
                                     .log();
            } //end try catch

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    UserController.LOGGER.atError()
                                         .withThrowable(e)
                                         .log();
                } //end try catch
            } //end if
        } //end try catch finally

        Map<String, ?> responseMap;

        if (rowsChanged == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "The user could not be created"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The user was successfully created"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //create

    /**
     * Attempts to read existing user data using the specified parameters. An ID is required for reading. Assuming a
     * user with the specified ID exists, their id and username are returned.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Map<String, ?>> read(@RequestParam Map<String, Object> parameters,
                                               @AuthenticationPrincipal String username) {
        System.out.println(username);

        String idKey = "id";

        String id = Utilities.getParameter(parameters, idKey, String.class);

        if (id == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "An ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user's data could not be retrieved"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String userQuery = """
            SELECT
                `id`,
                `username`
            FROM
                `users`
            WHERE
                `id` = ?""";

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        Map<String, ?> user;

        try {
            preparedStatement = connection.prepareStatement(userQuery);

            preparedStatement.setString(1, id);

            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                Map<String, ?> errorMap = Map.of(
                    "success", false,
                    "message", "A user with the specified ID could not be found"
                );

                return new ResponseEntity<>(errorMap, HttpStatus.OK);
            } //end if

            String rowId = resultSet.getString("id");

            String rowUsername = resultSet.getString("username");

            user = Map.of(
                "id", rowId,
                "username", rowUsername
            );
        } catch (SQLException e) {
            UserController.LOGGER.atError()
                                 .withThrowable(e)
                                 .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user's data could not be retrieved"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                UserController.LOGGER.atError()
                                     .withThrowable(e)
                                     .log();
            } //end try catch

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    UserController.LOGGER.atError()
                                         .withThrowable(e)
                                         .log();
                } //end try catch
            } //end if

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    UserController.LOGGER.atError()
                                         .withThrowable(e)
                                         .log();
                } //end try catch
            } //end if
        } //end try catch finally

        Map<String, ?> successMap = Map.of(
            "success", true,
            "user", user
        );

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //read

    /**
     * Attempts to update existing user data using the specified parameters. An ID is required for updating. A user's
     * username and password can be updated. At least one is required.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Map<String, ?>> update(@RequestBody Map<String, Object> parameters) {
        String idKey = "id";

        String id = Utilities.getParameter(parameters, idKey, String.class);

        if (id == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "An ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String usernameKey = "username";

        String username = Utilities.getParameter(parameters, usernameKey, String.class);

        Set<String> setStatements = new HashSet<>();

        List<String> arguments = new ArrayList<>();

        if ((username != null) && (username.length() > MAX_USERNAME_LENGTH)) {
            String message = "A username cannot exceed %d characters".formatted(MAX_USERNAME_LENGTH);

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", message
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } else if (username != null) {
            String setStatement = "    `username` = ?";

            setStatements.add(setStatement);

            arguments.add(username);
        } //end if

        String passwordKey = "password";

        String password = Utilities.getParameter(parameters, passwordKey, String.class);

        if (password != null) {
            String setStatement = "    `password_hash` = ?";

            String salt = BCrypt.gensalt();

            String passwordHash = BCrypt.hashpw(password, salt);

            setStatements.add(setStatement);

            arguments.add(passwordHash);
        } //end if

        arguments.add(id);

        if (setStatements.isEmpty()) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "At lease one update is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user's data could not be updated"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String updateUserStatementTemplate = """
            UPDATE `users`
            SET
            %s
            WHERE `id` = ?""";

        String setStatementsString = setStatements.stream()
                                                  .reduce("%s,\n%s"::formatted)
                                                  .get();

        String updateUserStatement = updateUserStatementTemplate.formatted(setStatementsString);

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(updateUserStatement);

            for (int i = 0; i < arguments.size(); i++) {
                int argumentIndex = i + 1;

                String argument = arguments.get(i);

                preparedStatement.setString(argumentIndex, argument);
            } //end for

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            UserController.LOGGER.atError()
                                 .withThrowable(e)
                                 .log();

            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The user's data could not be updated"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                UserController.LOGGER.atError()
                                     .withThrowable(e)
                                     .log();
            } //end try catch

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    UserController.LOGGER.atError()
                                         .withThrowable(e)
                                         .log();
                } //end try catch
            } //end if
        } //end try catch finally

        Map<String, ?> responseMap;

        if (rowsChanged == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "A user with the specified ID could not be found"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The user information was successfully updated"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete an existing user using the specified parameters. An ID is required for deletion.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the delete operation
     */
    @DeleteMapping
    public ResponseEntity<Map<String, ?>> delete(@RequestBody Map<String, Object> parameters) {
        String idKey = "id";

        String id = Utilities.getParameter(parameters, idKey, String.class);

        if (id == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "An ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The user could not be deleted"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String deleteUserStatement = """
            DELETE FROM `users`
            WHERE
                `id` = ?""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(deleteUserStatement);

            preparedStatement.setString(1, id);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            UserController.LOGGER.atError()
                                 .withThrowable(e)
                                 .log();

            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The user could not be deleted"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                UserController.LOGGER.atError()
                                     .withThrowable(e)
                                     .log();
            } //end try catch

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    UserController.LOGGER.atError()
                                         .withThrowable(e)
                                         .log();
                } //end try catch
            } //end if
        } //end try catch finally

        Map<String, ?> responseMap;

        if (rowsChanged == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "A user with the specified ID could not be found"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The user was successfully deleted"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //delete
}