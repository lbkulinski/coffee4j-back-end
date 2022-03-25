package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

/**
 * The REST controller used to interact with the Coffee4j schema data.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 25, 2022
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
     * Attempts to create a new schema using the specified parameters. A creator ID, default flag, and shared flag is
     * required for creation.
     *
     * @param parameters the parameters to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
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

        String id = UUID.randomUUID()
                        .toString();

        String insertSchemaStatement = """
            INSERT INTO `schemas` (
                `id`,
                `creator_id`,
                `default`,
                `shared`
            ) VALUES (
                ?,
                ?,
                ?,
                ?
            )""";

        PreparedStatement preparedStatement = null;

        int rowsChanged;

        try {
            preparedStatement = connection.prepareStatement(insertSchemaStatement);

            preparedStatement.setString(1, id);

            preparedStatement.setString(2, creatorId);

            preparedStatement.setBoolean(3, defaultFlag);

            preparedStatement.setBoolean(4, sharedFlag);

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
     * Attempts to read existing schema data using the specified parameters. Valid filters include a schema's id,
     * creator ID, default flag, and shared flag. A creator ID must be specified in order to use the default flag
     * filter. At least one filter is required. Assuming fields with the specified filters exist, a schema's ...
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
            String whereSubclause = "(`id` = ?)";

            whereSubclauses.add(whereSubclause);

            whereArguments.add(id);
        } //end if

        String creatorIdKey = "creator_id";

        String creatorId = Utilities.getParameter(parameters, creatorIdKey, String.class);

        if (creatorId != null) {
            String whereSubclause = "(`creator_id` = ?)";

            whereSubclauses.add(whereSubclause);

            whereArguments.add(creatorId);
        } //end if

        String defaultFlagKey = "default";

        Boolean defaultFlag = Utilities.getParameter(parameters, defaultFlagKey, Boolean.class);

        if ((creatorId == null) && (defaultFlag != null)) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "A creator ID is required to use the default flag filter"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } else if (defaultFlag != null) {
            String whereSubclause = "(`default` = ?)";

            whereSubclauses.add(whereSubclause);

            String defaultFlagString = defaultFlag ? "1" : "0";

            whereArguments.add(defaultFlagString);
        } //end if

        String sharedFlagKey = "shared";

        if (parameters.containsKey(sharedFlagKey)) {
            String whereSubclause = "(`shared` = '1')";

            whereSubclauses.add(whereSubclause);
        } //end if

        if (whereSubclauses.isEmpty()) {
            Map<String, ?> errorMap = Map.of(
                "success", false,
                "message", "At least one filter is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String whereSubclausesString = whereSubclauses.stream()
                                                      .reduce("%s\nAND %s"::formatted)
                                                      .get();

        String schemaQuery = """
            """;

        return new ResponseEntity<>(HttpStatus.OK);
    } //read
}