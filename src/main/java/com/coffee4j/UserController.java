package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> create(@RequestParam Map<String, Object> parameters) {
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

        return new ResponseEntity<>(HttpStatus.OK);
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
        } //end try catch

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //read
}