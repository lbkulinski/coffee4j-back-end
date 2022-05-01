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
import schema.generated.tables.FieldTypes;
import schema.generated.tables.Fields;
import schema.generated.tables.SchemaFields;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import schema.generated.tables.Schemas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

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
     * The {@code field_types} table of the {@link SchemaFieldsController} class.
     */
    private static final FieldTypes FIELD_TYPES;

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

        FIELD_TYPES = FieldTypes.FIELD_TYPES;

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
            String content = "An association with the specified parameters could not be created";

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

            String content = "An association with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "An association with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "An association with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //create

    private void processRecord(Record record, Map<Integer, Map<String, Object>> idToSchema,
                               Map<Integer, Set<Map<String, Object>>> idToFields) {
        Objects.requireNonNull(record, "the specified record is null");

        Objects.requireNonNull(idToSchema, "the specified map of ID to schema is null");

        Objects.requireNonNull(idToFields, "the specified map of ID to fields is null");

        int schemaId = record.get(SCHEMAS.ID);

        Map<String, Object> schema = idToSchema.computeIfAbsent(schemaId, id -> new HashMap<>());

        schema.putIfAbsent("id", schemaId);

        int schemaOwnerId = record.get(SCHEMAS.OWNER_ID);

        schema.putIfAbsent("owner_id", schemaOwnerId);

        String schemaName = record.get(SCHEMAS.NAME);

        schema.putIfAbsent("name", schemaName);

        boolean schemaDefault = record.get(SCHEMAS.DEFAULT);

        schema.putIfAbsent("default", schemaDefault);

        boolean schemaShared = record.get(SCHEMAS.SHARED);

        schema.putIfAbsent("shared", schemaShared);

        int fieldTypeId = record.get(FIELD_TYPES.ID);

        String fieldTypeName = record.get(FIELD_TYPES.NAME);

        int fieldId = record.get(FIELDS.ID);

        String fieldName = record.get(FIELDS.NAME);

        String fieldDisplayName = record.get(FIELDS.DISPLAY_NAME);

        boolean fieldShared = record.get(FIELDS.SHARED);

        Map<String, Object> fieldType = Map.of(
            "id", fieldTypeId,
            "name", fieldTypeName
        );

        Map<String, Object> field = Map.of(
            "id", fieldId,
            "name", fieldName,
            "display_name", fieldDisplayName,
            "shared", fieldShared,
            "type", fieldType
        );

        Set<Map<String, Object>> fields = idToFields.computeIfAbsent(schemaId, id -> new HashSet<>());

        fields.add(field);
    } //processRecord

    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(name = "schema_id", required = false) Integer schemaId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        List<Field<?>> columns = List.of(
            SCHEMAS.ID,
            SCHEMAS.OWNER_ID,
            SCHEMAS.NAME,
            SCHEMAS.DEFAULT,
            SCHEMAS.SHARED,
            FIELDS.ID,
            FIELDS.NAME,
            FIELDS.DISPLAY_NAME,
            FIELD_TYPES.ID,
            FIELD_TYPES.NAME
        );

        Condition condition;

        if (schemaId == null) {
            condition = DSL.noCondition();
        } else {
            condition = SCHEMAS.ID.eq(schemaId);
        } //end if

        int ownerId = user.id();

        Result<Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            result = context.select(columns)
                            .from(SCHEMAS)
                            .join(SCHEMA_FIELDS).on(SCHEMA_FIELDS.SCHEMA_ID.eq(SCHEMAS.ID))
                            .join(FIELDS).on(FIELDS.ID.eq(SCHEMA_FIELDS.FIELD_ID))
                            .join(FIELD_TYPES).on(FIELD_TYPES.ID.eq(FIELDS.TYPE_ID))
                            .where(condition)
                            .and(SCHEMAS.OWNER_ID.eq(ownerId)
                                                 .or(SCHEMAS.SHARED.isTrue()))
                            .and(FIELDS.OWNER_ID.eq(ownerId)
                                                .or(FIELDS.SHARED.isTrue()))
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The association's data could not be retrieved";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        Map<Integer, Map<String, Object>> idToSchema = new HashMap<>();

        Map<Integer, Set<Map<String, Object>>> idToFields = new HashMap<>();

        for (Record record : result) {
            this.processRecord(record, idToSchema, idToFields);
        } //end for

        for (Map.Entry<Integer, Map<String, Object>> entry : idToSchema.entrySet()) {
            int id = entry.getKey();

            if (!idToFields.containsKey(id)) {
                continue;
            } //end if

            Set<Map<String, Object>> fields = idToFields.get(id);

            Map<String, Object> schema = entry.getValue();

            schema.put("fields", fields);
        } //end for

        Collection<Map<String, Object>> values = idToSchema.values();

        Body<Collection<Map<String, Object>>> body = Body.success(values);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //read
}