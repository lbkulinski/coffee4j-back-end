package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schema.generated.tables.Fields;
import schema.generated.tables.SchemaFields;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import schema.generated.tables.Schemas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * The REST controller used to interact with the Coffee4j schema-field association data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version May 1, 2022
 */
@RestController
@RequestMapping("api/schema_fields")
public final class SchemaFieldsController {
    /**
     * The {@code schemas} table of the {@link SchemaFieldsController} class.
     */
    private static final Schemas SCHEMAS;

    /**
     * The {@code fields} table of the {@link SchemaFieldsController} class.
     */
    private static final Fields FIELDS;

    /**
     * The {@code schema_fields} table of the {@link SchemaFieldsController} class.
     */
    private static final SchemaFields SCHEMA_FIELDS;

    /**
     * The {@link Logger} of the {@link SchemaFieldsController} class.
     */
    private static final Logger LOGGER;

    static {
        SCHEMAS = Schemas.SCHEMAS;

        FIELDS = Fields.FIELDS;

        SCHEMA_FIELDS = SchemaFields.SCHEMA_FIELDS;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Returns whether the specified schema ID and field ID is associated with the specified owner ID.
     *
     * @param schemaId the schema ID to be used in the operation
     * @param fieldId the field ID to be used in the operation
     * @param ownerId the owner ID to be used in the operation
     * @return {@code true}, if the specified schema ID and field ID is associated with the specified owner ID and
     * {@code false} otherwise
     */
    private boolean checkOwner(int schemaId, int fieldId, int ownerId) {
        boolean schemaExists;

        boolean fieldExists;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            schemaExists = context.fetchExists(SCHEMAS, SCHEMAS.ID.eq(schemaId), SCHEMAS.OWNER_ID.eq(ownerId));

            fieldExists = context.fetchExists(FIELDS, FIELDS.ID.eq(fieldId), FIELDS.OWNER_ID.eq(ownerId));
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            return false;
        } //end try catch

        return schemaExists && fieldExists;
    } //checkOwner

    /**
     * Attempts to create a new schema-field association for the current logged-in user. A schema ID and field ID are
     * required for creation.
     *
     * @param schemaId the schema ID to be used in the operation
     * @param fieldId the field ID to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam("schema_id") int schemaId,
                                          @RequestParam("field_id") int fieldId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int ownerId = user.id();

        boolean ownerValid = this.checkOwner(schemaId, fieldId, ownerId);

        if (!ownerValid) {
            String content = "A schema-field association with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.insertInto(SCHEMA_FIELDS)
                                 .columns(SCHEMA_FIELDS.SCHEMA_ID, SCHEMA_FIELDS.FIELD_ID)
                                 .values(schemaId, fieldId)
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A schema-field association with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A schema-field association with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A schema-field association with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //create

    /**
     * Attempts to read the schema-field association data of the current logged-in user. Assuming data exists, the
     * schema ID and field ID of each schema-field association are returned.
     *
     * @param schemaId the schema ID to be used in the operation
     * @param fieldId the field ID to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(name = "schema_id", required = false) Integer schemaId,
                                        @RequestParam(name = "field_id", required = false) Integer fieldId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Condition condition = DSL.noCondition();

        if (schemaId != null) {
            condition = condition.and(SCHEMAS.ID.eq(schemaId));
        } //end if

        if (fieldId != null) {
            condition = condition.and(FIELDS.ID.eq(fieldId));
        } //end if

        int ownerId = user.id();

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            result = context.select(SCHEMA_FIELDS.SCHEMA_ID, SCHEMA_FIELDS.FIELD_ID)
                            .from(SCHEMAS)
                            .join(SCHEMA_FIELDS).on(SCHEMA_FIELDS.SCHEMA_ID.eq(SCHEMAS.ID))
                            .join(FIELDS).on(FIELDS.ID.eq(SCHEMA_FIELDS.FIELD_ID))
                            .where(condition)
                            .and(SCHEMAS.OWNER_ID.eq(ownerId)
                                                 .or(SCHEMAS.SHARED.isTrue()))
                            .and(FIELDS.OWNER_ID.eq(ownerId)
                                                .or(FIELDS.SHARED.isTrue()))
                            .fetch();

            System.out.println(context.select(SCHEMA_FIELDS.SCHEMA_ID, SCHEMA_FIELDS.FIELD_ID)
                                      .from(SCHEMAS)
                                      .join(SCHEMA_FIELDS).on(SCHEMA_FIELDS.SCHEMA_ID.eq(SCHEMAS.ID))
                                      .join(FIELDS).on(FIELDS.ID.eq(SCHEMA_FIELDS.FIELD_ID))
                                      .where(condition)
                                      .and(SCHEMAS.OWNER_ID.eq(ownerId)
                                                           .or(SCHEMAS.SHARED.isTrue()))
                                      .and(FIELDS.OWNER_ID.eq(ownerId)
                                                          .or(FIELDS.SHARED.isTrue()))
                                      .getSQL());
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The schema-field association's data could not be retrieved";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //read
}