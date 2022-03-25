package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

/**
 * The REST controller used to interact with the Coffee4j field data.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 25, 2022
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

    /**
     * Attempts to read existing field data using the specified parameters. Valid filters include a field's id, name,
     * type ID, and display name. Assuming fields with the specified filters exist, a field's name, display name, type
     * ID and type name are returned.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping("read")
    public ResponseEntity<Map<String, ?>> read(@RequestParam Map<String, Object> parameters) {
        String idKey = "id";

        String id = Utilities.getParameter(parameters, idKey, String.class);

        Set<String> whereSubclauses = new HashSet<>();

        List<String> whereArguments = new ArrayList<>();

        if (id != null) {
            String whereSubclause = "(`f`.`id` = ?)";

            whereSubclauses.add(whereSubclause);

            whereArguments.add(id);
        } //end if

        String nameKey = "name";

        String name = Utilities.getParameter(parameters, nameKey, String.class);

        if (name != null) {
            String whereSubclause = "(`f`.`name` = ?)";

            whereSubclauses.add(whereSubclause);

            whereArguments.add(name);
        } //end if

        String typeIdKey = "type_id";

        String typeId = Utilities.getParameter(parameters, typeIdKey, String.class);

        if (typeId != null) {
            String whereSubclause = "(`f`.`type_id` = ?)";

            whereSubclauses.add(whereSubclause);

            whereArguments.add(typeId);
        } //end if

        String displayNameKey = "display_name";

        String displayName = Utilities.getParameter(parameters, displayNameKey, String.class);

        if (displayName != null) {
            String whereSubclause = "(`f`.`display_name` = ?)";

            whereSubclauses.add(whereSubclause);

            whereArguments.add(displayName);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                    "success", false,
                    "message", "The field's data could not be updated"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String fieldQuery;

        if (whereSubclauses.isEmpty()) {
            fieldQuery = """
                SELECT
                    `f`.`name` AS `field_name`,
                    `f`.`display_name` AS `field_display_name`,
                    `ft`.`id` AS `type_id`,
                    `ft`.`name` AS `type_name`
                FROM
                    `fields` `f`
                        INNER JOIN
                    `field_types` `ft` ON `ft`.`id` = `f`.`type_id`""";
        } else {
            String whereSubclausesString = whereSubclauses.stream()
                                                          .reduce("%s\nAND%s"::formatted)
                                                          .get();

            fieldQuery = """
                SELECT
                    `f`.`name` AS `field_name`,
                    `f`.`display_name` AS `field_display_name`,
                    `ft`.`id` AS `type_id`,
                    `ft`.`name` AS `type_name`
                FROM
                    `fields` `f`
                        INNER JOIN
                    `field_types` `ft` ON `ft`.`id` = `f`.`type_id`
                WHERE
                    %s""".formatted(whereSubclausesString);
        } //end if

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        List<Map<String, String>> fields = new ArrayList<>();

        try {
            preparedStatement = connection.prepareStatement(fieldQuery);

            for (int i = 0; i < whereArguments.size(); i++) {
                int parameterIndex = i + 1;

                String argument = whereArguments.get(i);

                preparedStatement.setString(parameterIndex, argument);
            } //end for

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String rowFieldName = resultSet.getString("field_name");

                if (rowFieldName == null) {
                    continue;
                } //end if

                String rowFieldDisplayName = resultSet.getString("field_display_name");

                if (rowFieldDisplayName == null) {
                    continue;
                } //end if

                String rowTypeId = resultSet.getString("type_id");

                if (rowTypeId == null) {
                    continue;
                } //end if

                String rowTypeName = resultSet.getString("type_name");

                if (rowTypeName == null) {
                    continue;
                } //end if

                Map<String, String> field = Map.of(
                    "field_name", rowFieldName,
                    "field_display_name", rowFieldDisplayName,
                    "type_id", rowTypeId,
                    "type_name", rowTypeName
                );

                fields.add(field);
            } //end while
        } catch (SQLException e) {
            FieldController.LOGGER.atError()
                                  .withThrowable(e)
                                  .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The field's data could not be retrieved"
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

        Map<String, ?> responseMap;

        if (fields.isEmpty()) {
            responseMap = Map.of(
                "success", false,
                "message", "A field with the specified filters could not be found"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "fields", fields
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //read

    /**
     * Attempts to update existing field data using the specified parameters. An ID is required for updating. A field's
     * name, type ID, and display name can be updated. At least one is required.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PostMapping("update")
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

        String nameKey = "name";

        String name = Utilities.getParameter(parameters, nameKey, String.class);

        Set<String> setStatements = new HashSet<>();

        List<String> arguments = new ArrayList<>();

        if (name != null) {
            String setStatement = "    `name` = ?";

            setStatements.add(setStatement);

            arguments.add(name);
        } //end if

        String typeIdKey = "type_id";

        String typeId = Utilities.getParameter(parameters, typeIdKey, String.class);

        if (typeId != null) {
            String setStatement = "    `type_id` = ?";

            setStatements.add(setStatement);

            arguments.add(typeId);
        } //end if

        String displayNameKey = "display_name";

        String displayName = Utilities.getParameter(parameters, displayNameKey, String.class);

        if (displayName != null) {
            String setStatement = "    `display_name` = ?";

            setStatements.add(setStatement);

            arguments.add(displayName);
        } //end if

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
                "message", "The field's data could not be updated"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        arguments.add(id);

        String updateFieldStatementTemplate = """
            UPDATE `fields`
            SET
            %s
            WHERE
                `id` = ?""";

        String setStatementsString = setStatements.stream().reduce("%s,\n%s"::formatted).get();

        String updateFieldStatement = updateFieldStatementTemplate.formatted(setStatementsString);

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(updateFieldStatement);

            for (int i = 0; i < arguments.size(); i++) {
                int parameterIndex = i + 1;

                String argument = arguments.get(i);

                preparedStatement.setString(parameterIndex, argument);
            } //end for

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            FieldController.LOGGER.atError()
                                  .withThrowable(e)
                                  .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The field's data could not be updated"
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
                "message", "The field's data could not be updated"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The field's data was successfully updated"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete an existing field using the specified parameters. An ID is required for deletion.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the delete operation
     */
    @PostMapping("delete")
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
                "message", "The field could not be deleted"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String deleteFieldStatement = """
            DELETE FROM `fields`
            WHERE
                `id` = ?""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(deleteFieldStatement);

            preparedStatement.setString(1, id);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            FieldController.LOGGER.atError()
                                  .withThrowable(e)
                                  .log();

            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The field could not be deleted"
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
                "message", "A field with the specified ID could not be found"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The field was successfully deleted"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //delete
}