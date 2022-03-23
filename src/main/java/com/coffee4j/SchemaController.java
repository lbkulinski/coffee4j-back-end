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

    private String createSchema(Connection connection, String creatorId, boolean defaultFlag,
                                boolean sharedFlag) throws SQLException {
        Objects.requireNonNull(connection, "the specified connection is null");

        Objects.requireNonNull(creatorId, "the specified creator ID is null");

        String updateDefaultStatement = """
            UPDATE `schemas`
            SET
                `default` = '0'
            WHERE
                `creator_id` = ?""";

        String insertSchemaStatement = """
            INSERT INTO `schemas` (
                `creator_id`,
                `default`,
                `shared`
            ) VALUES (
                ?,
                ?,
                ?
            )""";

        PreparedStatement defaultPreparedStatement = null;

        PreparedStatement schemaPreparedStatement = null;

        ResultSet resultSet = null;

        String id;

        try {
            defaultPreparedStatement = connection.prepareStatement(updateDefaultStatement);

            defaultPreparedStatement.setString(1, creatorId);

            defaultPreparedStatement.executeUpdate();

            schemaPreparedStatement = connection.prepareStatement(insertSchemaStatement,
                                                                  Statement.RETURN_GENERATED_KEYS);

            schemaPreparedStatement.setString(1, creatorId);

            schemaPreparedStatement.setBoolean(2, defaultFlag);

            schemaPreparedStatement.setBoolean(3, sharedFlag);

            schemaPreparedStatement.executeUpdate();

            resultSet = schemaPreparedStatement.getGeneratedKeys();

            if (!resultSet.next()) {
                return null;
            } //end if

            int idIndex = 1;

            id = resultSet.getString(idIndex);
        } finally {
            if (defaultPreparedStatement != null) {
                try {
                    defaultPreparedStatement.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if

            if (schemaPreparedStatement != null) {
                try {
                    schemaPreparedStatement.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if
        } //end try catch finally

        return id;
    } //createSchema

    private Set<String> getValidTypeIds(Connection connection) throws SQLException {
        Objects.requireNonNull(connection, "the specified connection is null");

        String typeIdsQuery = """
            SELECT
                `id`
            FROM
                `field_types`""";

        Statement statement = null;

        ResultSet resultSet = null;

        Set<String> typeIds = new HashSet<>();

        try {
            statement = connection.createStatement();

            resultSet = statement.executeQuery(typeIdsQuery);

            while (resultSet.next()) {
                String typeId = resultSet.getString("id");

                typeIds.add(typeId);
            } //end while
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if
        } //end try catch finally

        return typeIds;
    } //getValidTypeIds

    private List<Map<String, String>> getFields(Connection connection,
                                                List<Map<String, String>> fields) throws SQLException {
        Objects.requireNonNull(connection, "the specified connection is null");

        Objects.requireNonNull(fields, "the specified List of fields is null");

        Set<String> validTypeIds = this.getValidTypeIds(connection);

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

    private Set<String> createFields(Connection connection, List<Map<String, String>> fields) throws SQLException {
        Objects.requireNonNull(connection, "the specified connection is null");

        Objects.requireNonNull(fields, "the specified List of fields is null");

        List<String> valuesPlaceholders = new ArrayList<>();

        List<String> valuesArguments = new ArrayList<>();

        for (Map<String, String> field : fields) {
            if (field == null) {
                continue;
            } //end if

            String name = field.get("name");

            if (name == null) {
                continue;
            } //end if

            String typeId = field.get("type_id");

            if (typeId == null) {
                continue;
            } //end if

            String displayName = field.get("display_name");

            if (displayName == null) {
                continue;
            } //end if

            String valuesPlaceholder = "(? , ? , ?)";

            valuesPlaceholders.add(valuesPlaceholder);

            valuesArguments.add(name);

            valuesArguments.add(typeId);

            valuesArguments.add(displayName);
        } //end for

        if (valuesPlaceholders.isEmpty()) {
            return null;
        } //end if

        String valuesPlaceholdersString = valuesPlaceholders.stream()
                                                            .reduce("%s,\n%s"::formatted)
                                                            .get();

        String insertFieldStatementTemplate = """
            INSERT INTO `fields` (
                `name`,
                `type_id`,
                `display_name`
            ) VALUES
            %s""";

        String insertFieldStatement = insertFieldStatementTemplate.formatted(valuesPlaceholdersString);

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        Set<String> ids = new HashSet<>();

        try {
            preparedStatement = connection.prepareStatement(insertFieldStatement, Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < valuesArguments.size(); i++) {
                int parameterIndex = i + 1;

                String valuesArgument = valuesArguments.get(i);

                preparedStatement.setString(parameterIndex, valuesArgument);
            } //end for

            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();

            while (resultSet.next()) {
                String id = resultSet.getString(1);

                ids.add(id);
            } //end while
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if
        } //end try catch

        return ids;
    } //createFields

    private boolean createSchemaFieldsAssociation(Connection connection, String schemaId,
                                                  Set<String> fieldIds) throws SQLException {
        Objects.requireNonNull(connection, "the specified connection is null");

        Objects.requireNonNull(schemaId, "the specified schema ID is null");

        Objects.requireNonNull(fieldIds, "the specified Set of field IDs is null");

        List<String> valuesPlaceholders = new ArrayList<>();

        List<String> valuesArguments = new ArrayList<>();

        for (String fieldId : fieldIds) {
            if (fieldId == null) {
                continue;
            } //end if

            String valuesPlaceholder = "(? , ?)";

            valuesPlaceholders.add(valuesPlaceholder);

            valuesArguments.add(schemaId);

            valuesArguments.add(fieldId);
        } //end for

        if (valuesPlaceholders.isEmpty()) {
            return false;
        } //end if

        String valuesPlaceholdersString = valuesPlaceholders.stream()
                                                            .reduce("%s,\n%s"::formatted)
                                                            .get();

        String insertAssociationStatementTemplate = """
            INSERT INTO `schema_fields` (
                `schema_id`,
                `field_id`
            )
            VALUES
            %s""";

        String insertAssociationStatement = insertAssociationStatementTemplate.formatted(valuesPlaceholdersString);

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(insertAssociationStatement);

            for (int i = 0; i < valuesArguments.size(); i++) {
                int parameterIndex = i + 1;

                String valueArgument = valuesArguments.get(i);

                preparedStatement.setString(parameterIndex, valueArgument);
            } //end for

            rowsChanged = preparedStatement.executeUpdate();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if
        } //end try catch finally

        return rowsChanged > 0;
    } //createSchemaFieldsAssociation

    private ResponseEntity<Map<String, ?>> createHelper(Connection connection, String creatorId, boolean defaultFlag,
                                                        boolean sharedFlag,
                                                        List<Map<String, String>> fields) throws SQLException {
        Objects.requireNonNull(connection, "the specified connection is null");

        Objects.requireNonNull(creatorId, "the specified creator ID is null");

        Objects.requireNonNull(fields, "the specified List of fields is null");

        connection.setAutoCommit(false);

        String schemaId = this.createSchema(connection, creatorId, defaultFlag, sharedFlag);

        if (schemaId == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The schema could not be created"
            );

            connection.rollback();

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        if (fields.isEmpty()) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "At least one field must be specified"
            );

            connection.rollback();

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        fields = this.getFields(connection, fields);

        if (fields == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The specified Set of fields is malformed"
            );

            connection.rollback();

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Set<String> fieldIds = this.createFields(connection, fields);

        if (fieldIds == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The fields could not be created"
            );

            connection.rollback();

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        boolean successful = this.createSchemaFieldsAssociation(connection, schemaId, fieldIds);

        Map<String, ?> responseMap;

        if (successful) {
            connection.commit();

            responseMap = Map.of(
                "success", false,
                "message", "The schema was successfully created"
            );
        } else {
            connection.rollback();

            responseMap = Map.of(
                "success", false,
                "message", "The schema could not be created"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //createHelper

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

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The schema could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        ResponseEntity<Map<String, ?>> responseEntity;

        try {
            responseEntity = this.createHelper(connection, creatorId, defaultFlag, sharedFlag, fields);
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The schema could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                SchemaController.LOGGER.atError()
                                       .withThrowable(e)
                                       .log();
            } //end try catch
        } //end try catch finally

        return responseEntity;
    } //create
}