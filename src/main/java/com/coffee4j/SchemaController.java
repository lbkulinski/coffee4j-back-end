package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("api/schemas")
public final class SchemaController {
    private static final Logger LOGGER;

    static {
        LOGGER = LogManager.getLogger();
    } //static

    private String createSchema(String creatorId, boolean defaultFlag, boolean sharedFlag) {
        Connection connection = Utilities.getConnection();

        if (connection == null) {
            return null;
        } //end if

        String updateDefaultStatement = """
            UPDATE `coffee_log_schemas`
            SET
                `default` = '0'
            WHERE
                `creator_id` = ?""";

        String insertSchemaStatement = """
            INSERT INTO `coffee_log_schemas` (
                `creator_id`,
                `default`,
                `shared`
            ) VALUES (
                ?,
                ?,
                ?
            )""";

        String id;

        try {
            PreparedStatement defaultPreparedStatement = connection.prepareStatement(updateDefaultStatement);

            defaultPreparedStatement.setString(1, creatorId);

            defaultPreparedStatement.executeUpdate();

            PreparedStatement schemaPreparedStatement = connection.prepareStatement(insertSchemaStatement,
                                                                                    Statement.RETURN_GENERATED_KEYS);

            schemaPreparedStatement.setString(1, creatorId);

            schemaPreparedStatement.setBoolean(2, defaultFlag);

            schemaPreparedStatement.setBoolean(3, sharedFlag);

            schemaPreparedStatement.executeUpdate();

            ResultSet resultSet = schemaPreparedStatement.getGeneratedKeys();

            if (!resultSet.next()) {
                return null;
            } //end if

            int idIndex = 1;

            id = resultSet.getString(idIndex);
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            return null;
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                SchemaController.LOGGER.atError()
                                       .withThrowable(e)
                                       .log();
            } //end try catch
        } //end try catch finally

        return id;
    } //createSchema

    private Set<String> getValidTypeIds() {
        Connection connection = Utilities.getConnection();

        if (connection == null) {
            return Set.of();
        } //end if

        String typeIdsQuery = """
            SELECT
                `id`
            FROM
                `coffee_log_field_types`""";

        Set<String> typeIds = new HashSet<>();

        try {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(typeIdsQuery);

            while (resultSet.next()) {
                String typeId = resultSet.getString("id");

                typeIds.add(typeId);
            } //end while
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            return Set.of();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                SchemaController.LOGGER.atError()
                                       .withThrowable(e)
                                       .log();
            } //end try catch
        } //end try catch finally

        return typeIds;
    } //getValidTypeIds

    private List<Map<String, String>> getFields(List<Map<String, String>> fields) {
        Objects.requireNonNull(fields, "the specified Set of fields is null");

        Set<String> validTypeIds = this.getValidTypeIds();

        if (validTypeIds == null) {
            return null;
        } //end if

        List<Map<String, String>> fieldsCopy = new ArrayList<>();

        for (Map<String, ?> field : fields) {
            Object nameObject = field.get("name");

            if (!(nameObject instanceof String name)) {
                return null;
            } //end if

            Object typeIdObject = field.get("type_id");

            if (!(typeIdObject instanceof String typeId)) {
                return null;
            } //end if

            if (!validTypeIds.contains(typeId)) {
                return null;
            } //end if

            Object displayNameObject = field.get("display_name");

            if (!(displayNameObject instanceof String displayName)) {
                return null;
            } //end if

            Map<String, String> fieldCopy = Map.of(
                "name", name,
                "type_id", typeId,
                "display_name", displayName
            );

            fieldsCopy.add(fieldCopy);
        } //end for

        return Collections.unmodifiableList(fieldsCopy);
    } //getFields

    private String createField(String name, String typeId, String displayName) {
        Connection connection = Utilities.getConnection();

        if (connection == null) {
            return null;
        } //end if

        String insertFieldStatementTemplate = """
            INSERT INTO `coffee_log_fields` (
                `name`,
                `type_id`,
                `display_name`
            ) VALUES
            %s
            """;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertFieldStatementTemplate,
                                                                              Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            return null;
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                SchemaController.LOGGER.atError()
                                       .withThrowable(e)
                                       .log();
            } //end try catch
        } //end try catch

        return null;
    } //createField

    @PostMapping("create")
    public ResponseEntity<Map<String, ?>> create(@RequestBody Map<String, Object> parameters) {
        String creatorIdKey = "creator_id";

        String creatorId = Utilities.getParameter(parameters, creatorIdKey, String.class);

        if (creatorId == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A creator ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String defaultKey = "default";

        Boolean defaultFlag = Utilities.getParameter(parameters, defaultKey, Boolean.class);

        if (defaultFlag == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A default flag is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String sharedKey = "shared";

        Boolean sharedFlag = Utilities.getParameter(parameters, sharedKey, Boolean.class);

        if (sharedFlag == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A shared flag is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String schemaId = this.createSchema(creatorId, defaultFlag, sharedFlag);

        if (schemaId == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The schema could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String fieldsKey = "fields";

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fields = Utilities.getParameter(parameters, fieldsKey, List.class);

        if (fields == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A Set of fields is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        if (fields.isEmpty()) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "At least one field must be specified"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        fields = this.getFields(fields);

        if (fields == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The specified Set of fields is malformed"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        return new ResponseEntity<>(HttpStatus.OK);
    } //create
}