package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.*;

/**
 * The REST controller used to interact with the Coffee4j field data.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 24, 2022
 */
@RestController
@RequestMapping("api/fields")
public class FieldController {
    /**
     * The {@link Logger} of the {@link SchemaController} class.
     */
    private static final Logger LOGGER;

    static {
        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Returns the valid type IDs using the specified connection or {@code null} if an error occurred.
     *
     * @param connection the connection to be used in the operation
     * @return the valid type IDs using the specified connection or {@code null} if an error occurred
     * @throws NullPointerException if the specified connection is {@code null}
     */
    private Set<String> getValidTypeIds(Connection connection) {
        Objects.requireNonNull(connection, "the specified connection is null");

        String typeIdsQuery = """
            SELECT
                `id`
            FROM
                `field_types`""";

        Statement statement = null;

        ResultSet resultSet = null;

        Set<String> ids = new HashSet<>();

        try {
            statement = connection.createStatement();

            resultSet = statement.executeQuery(typeIdsQuery);

            while (resultSet.next()) {
                String id = resultSet.getString("id");

                ids.add(id);
            } //end while
        } catch (SQLException e) {
            FieldController.LOGGER.atError()
                                  .withThrowable(e)
                                  .log();

            return null;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    FieldController.LOGGER.atError()
                                          .withThrowable(e)
                                          .log();
                } //end try catch
            } //end if

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    FieldController.LOGGER.atError()
                                          .withThrowable(e)
                                          .log();
                } //end try catch
            } //end if
        } //end try catch finally

        return Collections.unmodifiableSet(ids);
    } //getValidTypeIds

    /**
     * Attempts to create a new field using the specified parameters. A name, type ID, and display name are required
     * for creation.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping("create")
    public ResponseEntity<Map<String, ?>> create(@RequestBody Map<String, Object> parameters) {
        String nameKey = "name";

        String name = Utilities.getParameter(parameters, nameKey, String.class);

        if (name == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String typeIdKey = "type_id";

        String typeId = Utilities.getParameter(parameters, typeIdKey, String.class);

        if (typeId == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A type ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The field could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        Set<String> validTypeIds = this.getValidTypeIds(connection);

        if (validTypeIds == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The field could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        if (!validTypeIds.contains(typeId)) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The specified type ID is invalid"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String displayNameKey = "display_name";

        String displayName = Utilities.getParameter(parameters, displayNameKey, String.class);

        if (displayName == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A display name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String id = UUID.randomUUID()
                        .toString();

        String insertFieldStatement = """
            INSERT INTO `fields` (
                `id`,
                `name`,
                `type_id`,
                `display_name`
            ) VALUES (
                ?,
                ?,
                ?,
                ?
            )""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(insertFieldStatement);

            preparedStatement.setString(1, id);

            preparedStatement.setString(2, name);

            preparedStatement.setString(3, typeId);

            preparedStatement.setString(4, displayName);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            FieldController.LOGGER.atError()
                                  .withThrowable(e)
                                  .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The field could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                FieldController.LOGGER.atError()
                                      .withThrowable(e)
                                      .log();
            } //end try catch

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    FieldController.LOGGER.atError()
                                          .withThrowable(e)
                                          .log();
                } //end try catch
            } //end if
        } //end try catch finally

        Map<String, ?> responseMap;

        if (rowsChanged == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "The field could not be created"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The field was successfully created"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //create
}