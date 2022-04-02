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
 * The REST controller used to interact with the Coffee4j field data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 1, 2022
 */
@RestController
@RequestMapping("api/fields")
public final class FieldController {
    /**
     * The maximum name length of the {@link FieldController} class.
     */
    private static final int MAX_NAME_LENGTH;

    /**
     * The {@link Logger} of the {@link FieldController} class.
     */
    private static final Logger LOGGER;

    static {
        MAX_NAME_LENGTH = 45;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to create a new field using the specified parameters. A name, type ID, display name, and shared flag
     * are required for creation.
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

        String nameKey = "name";

        String name = Utilities.getParameter(parameters, nameKey, String.class);

        if (name == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } else if (name.length() > FieldController.MAX_NAME_LENGTH) {
            String message = "A name cannot exceed %d characters".formatted(FieldController.MAX_NAME_LENGTH);

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", message
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String typeIdKey = "type_id";

        Integer typeId = Utilities.getParameter(parameters, typeIdKey, Integer.class);

        if (typeId == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A type ID is required"
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
        } else if (displayName.length() > FieldController.MAX_NAME_LENGTH) {
            String message = "A display name cannot exceed %d characters".formatted(FieldController.MAX_NAME_LENGTH);

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", message
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
                "message", "The field could not be created"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String insertFieldStatement = """
            INSERT INTO `fields` (
                `creator_id`,
                `name`,
                `type_id`,
                `display_name`,
                `shared`
            ) VALUES (
                ?,
                ?,
                ?,
                ?,
                ?
            )""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(insertFieldStatement);

            preparedStatement.setInt(1, creatorId);

            preparedStatement.setString(2, name);

            preparedStatement.setInt(3, typeId);

            preparedStatement.setString(4, displayName);

            preparedStatement.setBoolean(5, sharedFlag);

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
     * Attempts to read the field data of the current logged-in user using the specified parameters. Assuming data
     * exists, the ID, name, type ID, and display name of each field are returned.
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
                FieldController.LOGGER.atError()
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

        String nameKey = "name";

        String name = Utilities.getParameter(parameters, nameKey, String.class);

        if (name != null) {
            String subclause = "(`name` = ?)";

            whereSubclauses.add(subclause);

            whereArguments.add(name);
        } //end if

        String typeIdKey = "type_id";

        String typeId = Utilities.getParameter(parameters, typeIdKey, String.class);

        if (typeId != null) {
            String subclause = "(`type_id` = ?)";

            whereSubclauses.add(subclause);

            whereArguments.add(typeId);
        } //end if

        String displayNameKey = "display_name";

        String displayName = Utilities.getParameter(parameters, displayNameKey, String.class);

        if (displayName != null) {
            String subclause = "(`display_name` = ?)";

            whereSubclauses.add(subclause);

            whereArguments.add(displayName);
        } //end if

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "The field's data could not be retrieved"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String whereClause = whereSubclauses.stream()
                                            .reduce("%s\nAND %s"::formatted)
                                            .get();

        String fieldQuery = """
            SELECT
                `id`,
                `creator_id`,
                `name`,
                `type_id`,
                `display_name`
            FROM
                `fields`
            WHERE
            %s""".formatted(whereClause);

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        Set<Map<String, ?>> fieldData = new HashSet<>();

        try {
            preparedStatement = connection.prepareStatement(fieldQuery);

            for (int i = 0; i < whereArguments.size(); i++) {
                int parameterIndex = i + 1;

                Object argument = whereArguments.get(i);

                preparedStatement.setObject(parameterIndex, argument);
            } //end for

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int rowId = resultSet.getInt("id");

                int rowCreatorId = resultSet.getInt("creator_id");

                String rowName = resultSet.getString("name");

                int rowTypeId = resultSet.getInt("type_id");

                String rowDisplayName = resultSet.getString("display_name");

                Map<String, ?> fieldDatum = Map.of(
                    "id", rowId,
                    "creator_id", rowCreatorId,
                    "name", rowName,
                    "type_id", rowTypeId,
                    "display_name", rowDisplayName
                );

                fieldData.add(fieldDatum);
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

        Map<String, ?> successMap = Map.of(
            "success", true,
            "fields", fieldData
        );

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the field data of the current logged-in user using the specified parameters. A field ID is
     * required for updating. A field's name, type ID, display name, and shared flag can be updated. At least one
     * update is required.
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

        String nameKey = "name";

        String name = Utilities.getParameter(parameters, nameKey, String.class);

        Set<String> setStatements = new LinkedHashSet<>();

        List<Object> arguments = new ArrayList<>();

        if ((name != null) && (name.length() > FieldController.MAX_NAME_LENGTH)) {
            String message = "A name cannot exceed %d characters".formatted(FieldController.MAX_NAME_LENGTH);

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", message
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } else if (name != null) {
            String setStatement = "    `name` = ?";

            setStatements.add(setStatement);

            arguments.add(name);
        } //end if

        String typeIdKey = "type_id";

        Integer typeId = Utilities.getParameter(parameters, typeIdKey, Integer.class);

        if (typeId != null) {
            String setStatement = "    `type_id` = ?";

            setStatements.add(setStatement);

            arguments.add(typeId);
        } //end if

        String displayNameKey = "display_name";

        String displayName = Utilities.getParameter(parameters, displayNameKey, String.class);

        if ((displayName != null) && (displayName.length() > FieldController.MAX_NAME_LENGTH)) {
            String message = "A display name cannot exceed %d characters".formatted(FieldController.MAX_NAME_LENGTH);

            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", message
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } else if (displayName != null) {
            String setStatement = "    `display_name` = ?";

            setStatements.add(setStatement);

            arguments.add(displayName);
        } //end if

        String sharedFlagKey = "shared";

        Boolean sharedFlag = Utilities.getParameter(parameters, sharedFlagKey, Boolean.class);

        if (sharedFlag != null) {
            String setStatement = "    `shared` = ?";

            setStatements.add(setStatement);

            arguments.add(sharedFlag);
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
                "message", "The field's data could not be updated"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String setStatementsString = setStatements.stream()
                                                  .reduce("%s,\n%s"::formatted)
                                                  .get();

        String updateFieldStatement = """
            UPDATE `fields`
            SET
            %s
            WHERE
                (`id` = ?)
                    AND (`creator_id` = ?)""".formatted(setStatementsString);

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(updateFieldStatement);

            for (int i = 0; i < arguments.size(); i++) {
                int parameterIndex = i + 1;

                Object argument = arguments.get(i);

                preparedStatement.setObject(parameterIndex, argument);
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
                "message", "A field with the specified ID and creator ID could not be found"
            );
        } else {
            responseMap = Map.of(
                "success", true,
                "message", "The field information was successfully updated"
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the field data of the current logged-in user using the specified parameters. A field ID is
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
                "message", "The field could not be deleted"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String deleteFieldStatement = """
            DELETE FROM `fields`
            WHERE
                (`id` = ?)
                    AND (`creator_id` = ?)""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(deleteFieldStatement);

            preparedStatement.setInt(1, id);

            preparedStatement.setInt(2, creatorId);

            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            FieldController.LOGGER.atError()
                                  .withThrowable(e)
                                  .log();

            Map<String, ?> errorMap = Map.of(
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
                "message", "A field with the specified ID and creator ID could not be found"
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