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
import java.util.Map;

/**
 * The REST controller used to interact with the Coffee4j user data.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 21, 2022
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

    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> parameters) {
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
                "message", "The user could not be created"
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
            )
            """;

        int rowChangeCount;

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

            rowChangeCount = preparedStatement.executeUpdate();
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

        if (rowChangeCount == 0) {
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

    @GetMapping("read")
    public ResponseEntity<?> read(@RequestParam Map<String, Object> parameters) {
        String idKey = "id";

        String idString = Utilities.getParameter(parameters, idKey, String.class);

        if (idString == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "An ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        int id;

        try {
            id = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The specified ID must be an integer"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end try catch

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
                `first_name`,
                `last_name`,
                `email`
            FROM
                `coffee_log_users`
            WHERE
                `id` = ?
            """;

        ResultSet resultSet;

        Map<String, ?> successMap;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(userQuery);

            int idIndex = 1;

            preparedStatement.setInt(idIndex, id);

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
}