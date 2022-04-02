package com.coffee4j.controller;

import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The REST controller used to interact with the Coffee4j schema data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 1, 2022
 */
@RestController
@RequestMapping("api/schemas")
public final class SchemaController {
    /**
     * The {@link Logger} of the {@link SchemaController} class.
     */
    private static final Logger LOGGER;

    static {
        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to create a new schema using the specified parameters. A default flag and shared flag are required for
     * creation.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Map<String, ?>> create(@RequestBody Map<String, Object> parameters) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        String defaultFlagKey = "default";

        Boolean defaultFlag = Utilities.getParameter(parameters, defaultFlagKey, Boolean.class);

        if (defaultFlag == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A default flag is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String sharedFlagKey = "shared";

        Boolean sharedFlag = Utilities.getParameter(parameters, sharedFlagKey, Boolean.class);

        if (sharedFlag == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A shared flag is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The schema could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

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

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(insertSchemaStatement);

            preparedStatement.setInt(1, creatorId);

            preparedStatement.setBoolean(2, defaultFlag);

            preparedStatement.setBoolean(3, sharedFlag);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            Map<String, ?> errorMap = Map.of(
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

        Map<String, ?> responseMap;

        if (rowsChanged == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "The schema could not be created"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The schema was successfully created"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //create

    /**
     * Attempts to read the schema data of the current logged-in user using the specified parameters. Assuming data
     * exists, the ID, creator ID, default flag, and shared flag of each schema are returned.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Map<String, ?>> read(@RequestParam Map<String, Object> parameters) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        String sharedFlagKey = "shared";

        String sharedFlagString = Utilities.getParameter(parameters, sharedFlagKey, String.class);

        Boolean sharedFlag = null;

        if (sharedFlagString != null) {
            sharedFlag = Boolean.parseBoolean(sharedFlagString);
        } //end if

        Set<String> whereSubclauses = new LinkedHashSet<>();

        List<Object> whereArguments = new ArrayList<>();

        if ((sharedFlag == null) || !sharedFlag) {
            String subclause = "(`creator_id` = ?)";

            whereSubclauses.add(subclause);

            whereArguments.add(creatorId);
        } //end if

        if (sharedFlag != null) {
            String subclause = "(`shared` = ?)";

            whereSubclauses.add(subclause);

            whereArguments.add(sharedFlag);
        } //end if

        String idKey = "id";

        String idString = Utilities.getParameter(parameters, idKey, String.class);

        if (idString != null) {
            int id;

            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException e) {
                SchemaController.LOGGER.atError()
                                       .withThrowable(e)
                                       .log();

                Map<String, ?> errorMap = Map.of(
                    "success", false,
                    "message", "The specified ID is not a valid int"
                );

                return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
            } //end try catch

            String idSubclause = "(`id` = ?)";

            whereSubclauses.add(idSubclause);

            whereArguments.add(id);
        } //end if

        String defaultFlagKey = "default";

        String defaultFlagString = Utilities.getParameter(parameters, defaultFlagKey, String.class);

        if (defaultFlagString != null) {
            String defaultFlagSubclause = "(`default` = ?)";

            whereSubclauses.add(defaultFlagSubclause);

            boolean defaultFlag = Boolean.parseBoolean(defaultFlagString);

            whereArguments.add(defaultFlag);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The schema's data could not be retrieved"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String whereClause = whereSubclauses.stream()
                                            .reduce("%s\nAND %s"::formatted)
                                            .get();

        String schemaQuery = """
            SELECT
                `id`, `creator_id`, `default`, `shared`
            FROM
                `schemas`
            WHERE
            %s""".formatted(whereClause);

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        Set<Map<String, ?>> schemaData = new HashSet<>();

        try {
            preparedStatement = connection.prepareStatement(schemaQuery);

            for (int i = 0; i < whereArguments.size(); i++) {
                int parameterIndex = i + 1;

                Object argument = whereArguments.get(i);

                preparedStatement.setObject(parameterIndex, argument);
            } //end for

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int rowId = resultSet.getInt("id");

                int rowCreatorId = resultSet.getInt("creator_id");

                boolean rowDefaultFlag = resultSet.getBoolean("default");

                boolean rowSharedFlag = resultSet.getBoolean("shared");

                Map<String, ?> schemaDatum = Map.of(
                    "id", rowId,
                    "creator_id", rowCreatorId,
                    "default", rowDefaultFlag,
                    "shared", rowSharedFlag
                );

                schemaData.add(schemaDatum);
            } //end while
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The schema's data could not be retrieved"
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
        } //end try catch finally

        Map<String, ?> successMap = Map.of(
            "success", true,
            "schemas", schemaData
        );

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the schema data of the current logged-in user using the specified parameters. A schema ID is
     * required for updating. A schema's default flag and shared flag can be updated. At least one update is required.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Map<String, ?>> update(@RequestBody Map<String, Object> parameters) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        String idKey = "id";

        Integer id = Utilities.getParameter(parameters, idKey, Integer.class);

        if (id == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A schema ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String defaultFlagKey = "default";

        Boolean defaultFlag = Utilities.getParameter(parameters, defaultFlagKey, Boolean.class);

        Set<String> setStatements = new LinkedHashSet<>();

        List<Object> arguments = new ArrayList<>();

        if (defaultFlag != null) {
            String setStatement = "    `default` = ?";

            setStatements.add(setStatement);

            String defaultFlagString = defaultFlag ? "1" : "0";

            arguments.add(defaultFlagString);
        } //end if

        String sharedFlagKey = "shared";

        Boolean sharedFlag = Utilities.getParameter(parameters, sharedFlagKey, Boolean.class);

        if (sharedFlag != null) {
            String setStatement = "    `shared` = ?";

            setStatements.add(setStatement);

            String sharedFlagString = sharedFlag ? "1" : "0";

            arguments.add(sharedFlagString);
        } //end if

        if (setStatements.isEmpty()) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "At lease one update is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        arguments.add(id);

        arguments.add(creatorId);

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The schema's data could not be updated"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String setStatementsString = setStatements.stream()
                                                  .reduce("%s,\n%s"::formatted)
                                                  .get();

        String updateSchemaStatement = """
            UPDATE `schemas`
            SET
            %s
            WHERE
                (`id` = ?)
                    AND (`creator_id` = ?)""".formatted(setStatementsString);

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(updateSchemaStatement);

            for (int i = 0; i < arguments.size(); i++) {
                int parameterIndex = i + 1;

                Object argument = arguments.get(i);

                preparedStatement.setObject(parameterIndex, argument);
            } //end for

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The schema's data could not be updated"
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

        Map<String, ?> responseMap;

        if (rowsChanged == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "A schema with the specified ID and creator ID could not be found"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The schema information was successfully updated"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the schema data of the current logged-in user using the specified parameters. A schema ID is
     * required for deletion.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the delete operation
     */
    @DeleteMapping
    public ResponseEntity<Map<String, ?>> delete(@RequestBody Map<String, Object> parameters) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        String idKey = "id";

        Integer id = Utilities.getParameter(parameters, idKey, Integer.class);

        if (id == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A schema ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The schema could not be deleted"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String deleteUserStatement = """
            DELETE FROM `schemas`
            WHERE
                (`id` = ?)
                    AND (`creator_id` = ?)""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(deleteUserStatement);

            preparedStatement.setInt(1, id);

            preparedStatement.setInt(2, creatorId);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SchemaController.LOGGER.atError()
                                   .withThrowable(e)
                                   .log();

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The schema could not be deleted"
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

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    SchemaController.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();
                } //end try catch
            } //end if
        } //end try catch

        Map<String, ?> responseMap;

        if (rowsChanged == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "A schema with the specified ID and creator ID could not be found"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The schema was successfully deleted"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //delete
}