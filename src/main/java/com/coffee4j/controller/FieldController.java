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
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version April 1, 2022
 */
@RestController
@RequestMapping("api/fields")
public final class FieldController {
    /**
     * The {@link Logger} of the {@link FieldController} class.
     */
    private static final Logger LOGGER;

    static {
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

        String displayNameKey = "display_name";

        String displayName = Utilities.getParameter(parameters, displayNameKey, String.class);

        if (displayName == null) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A display name is required"
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

            preparedStatement.setString(3, typeId);

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

        if (!(principal instanceof User)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        String idKey = "id";

        String id = Utilities.getParameter(parameters, idKey, String.class);

        Set<String> whereSubclauses = new HashSet<>();

        List<String> whereArguments = new ArrayList<>();

        if (id != null) {
            String subclause = "(`id` = ?)";

            whereSubclauses.add(subclause);

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

        String fieldQuery = """
            SELECT
                `id`,
                `name`,
                `type_id`,
                `display_name`
            FROM
                `fields`""";

        if (!whereSubclauses.isEmpty()) {
            String whereClause = whereSubclauses.stream()
                                                .reduce("%s\nAND %s"::formatted)
                                                .get();

            fieldQuery += """
                
                WHERE
                %s""".formatted(whereClause);
        } //end if

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        Set<Map<String, ?>> fieldData = new HashSet<>();

        try {
            preparedStatement = connection.prepareStatement(fieldQuery);

            for (int i = 0; i < whereArguments.size(); i++) {
                int parameterIndex = i + 1;

                String argument = whereArguments.get(i);

                preparedStatement.setString(parameterIndex, argument);
            } //end for

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String rowId = resultSet.getString("id");

                String rowName = resultSet.getString("name");

                String rowTypeId = resultSet.getString("type_id");

                String rowDisplayName = resultSet.getString("display_name");

                Map<String, ?> fieldDatum = Map.of(
                    "id", rowId,
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
}