package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The REST controller used to interact with the Coffee4j user data.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 22, 2022
 */
@RestController
@RequestMapping("api/users")
public final class UserController {
    /**
     * The {@link Logger} of the {@link UserController} class.
     */
    private static final Logger LOGGER;

    static {
        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to create a new user using the specified parameters. A first name, last name, email, and password are
     * required for creation.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping("create")
    public ResponseEntity<Map<String, ?>> create(@RequestBody Map<String, Object> parameters) {
        String firstNameKey = "first_name";

        String firstName = Utilities.getParameter(parameters, firstNameKey, String.class);

        if (firstName == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A first name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String lastNameKey = "last_name";

        String lastName = Utilities.getParameter(parameters, lastNameKey, String.class);

        if (lastName == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A last name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String emailKey = "email";

        String email = Utilities.getParameter(parameters, emailKey, String.class);

        if (email == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "An email is required"
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
                "message", "A connection to the database could not be established"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String userInsertStatement = """
            INSERT INTO `coffee_log_users` (
                `first_name`,
                `last_name`,
                `email`,
                `password_hash`
            ) VALUES (
                ?,
                ?,
                ?,
                ?
            )""";

        int rowsChanged;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(userInsertStatement);

            int firstNameIndex = 1;

            preparedStatement.setString(firstNameIndex, firstName);

            int lastNameIndex = 2;

            preparedStatement.setString(lastNameIndex, lastName);

            int emailIndex = 3;

            preparedStatement.setString(emailIndex, email);

            int passwordHashIndex = 4;

            preparedStatement.setString(passwordHashIndex, passwordHash);

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
     * Attempts to read existing user data using the specified parameters. An ID is required for reading.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping("read")
    public ResponseEntity<?> read(@RequestParam Map<String, Object> parameters) {
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
                "message", "A connection to the database could not be established"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String userQuery = """
            SELECT
                `id`,
                `first_name`,
                `last_name`,
                `email`
            FROM
                `coffee_log_users`
            WHERE
                `id` = ?""";

        ResultSet resultSet;

        Map<String, ?> successMap;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(userQuery);

            int idIndex = 1;

            preparedStatement.setString(idIndex, id);

            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                Map<String, ?> errorMap = Map.of(
                    "success", false,
                    "message", "A user with the specified ID could not be found"
                );

                return new ResponseEntity<>(errorMap, HttpStatus.OK);
            } //end if

            int userId = resultSet.getInt("id");

            String firstName = resultSet.getString("first_name");

            String lastName = resultSet.getString("last_name");

            String email = resultSet.getString("email");

            Map<String, ?> dataMap = Map.of(
                "id", userId,
                "first_name", firstName,
                "last_name", lastName,
                "email", email
            );

            successMap = Map.of(
                "success", true,
                "data", dataMap
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
        } //end try catch finally

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //read

    /**
     * Attempts to update existing user data using the specified parameters. An ID is required for updating. A user's
     * first name, last name, email, and password can be updated. At least one is required.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PostMapping("update")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> parameters) {
        String idKey = "id";

        String id = Utilities.getParameter(parameters, idKey, String.class);

        if (id == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "An ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String firstNameKey = "first_name";

        String firstName = Utilities.getParameter(parameters, firstNameKey, String.class);

        Set<String> setStatements = new HashSet<>();

        List<String> arguments = new ArrayList<>();

        if (firstName != null) {
            String setStatement = "    `first_name` = ?";

            setStatements.add(setStatement);

            arguments.add(firstName);
        } //end if

        String lastNameKey = "last_name";

        String lastName = Utilities.getParameter(parameters, lastNameKey, String.class);

        if (lastName != null) {
            String setStatement = "    `last_name` = ?";

            setStatements.add(setStatement);

            arguments.add(lastName);
        } //end if

        String emailKey = "email";

        String email = Utilities.getParameter(parameters, emailKey, String.class);

        if (email != null) {
            String setStatement = "    `email` = ?";

            setStatements.add(setStatement);

            arguments.add(email);
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
                "message", "A connection to the database could not be established"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String userUpdateStatementTemplate = """
            UPDATE `coffee_log_users`
            SET
            %s
            WHERE `id` = ?""";

        String setStatementsString = setStatements.stream()
                                                  .reduce("%s,\n%s"::formatted)
                                                  .get();

        String userUpdateStatement = userUpdateStatementTemplate.formatted(setStatementsString);

        int rowsChanged;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(userUpdateStatement);

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

            return new ResponseEntity<>(errorMap, HttpStatus.OK);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                UserController.LOGGER.atError()
                                     .withThrowable(e)
                                     .log();
            } //end try catch
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
    @PostMapping("delete")
    public ResponseEntity<?> delete(@RequestBody Map<String, Object> parameters) {
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
                "message", "A connection to the database could not be established"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String userDeleteStatement = """
            DELETE FROM `coffee_log_users`
            WHERE
                `id` = ?""";

        int rowsChanged;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(userDeleteStatement);

            int idIndex = 1;

            preparedStatement.setString(idIndex, id);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            UserController.LOGGER.atError()
                                 .withThrowable(e)
                                 .log();

            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The user could not be deleted"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.OK);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                UserController.LOGGER.atError()
                                     .withThrowable(e)
                                     .log();
            } //end try catch
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