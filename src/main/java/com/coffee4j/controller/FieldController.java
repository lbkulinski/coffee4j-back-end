package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schema.generated.tables.FieldTypes;
import schema.generated.tables.Fields;
import schema.generated.tables.Schemas;
import schema.generated.tables.Users;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The REST controller used to interact with the Coffee4j field data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 26, 2022
 */
@RestController
@RequestMapping("api/fields")
public final class FieldController {
    /**
     * The {@code schemas} table of the {@link FieldController} class.
     */
    private static final Schemas SCHEMAS;

    /**
     * The {@code fields} table of the {@link FieldController} class.
     */
    private static final Fields FIELDS;

    /**
     * The maximum name length of the {@link FieldController} class.
     */
    private static final int MAX_NAME_LENGTH;

    /**
     * The {@link Logger} of the {@link FieldController} class.
     */
    private static final Logger LOGGER;

    static {
        SCHEMAS = Schemas.SCHEMAS;

        FIELDS = Fields.FIELDS;

        MAX_NAME_LENGTH = 45;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Returns whether a schema with the specified schema ID and creator ID does not exist.
     *
     * @param schemaId the schema ID to be used in the operation
     * @param creatorId the creator ID to be used in the operation
     * @return {@code true}, if a schema with the specified schema ID and creator ID does not exist and {@code false}
     * otherwise
     */
    private boolean checkSchema(int schemaId, int creatorId) {
        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.selectCount()
                            .from(SCHEMAS)
                            .where(SCHEMAS.ID.eq(schemaId))
                            .and(SCHEMAS.CREATOR_ID.eq(creatorId))
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            return true;
        } //end try catch

        if (record == null) {
            return true;
        } //end if

        int count = record.get(0, Integer.class);

        return count == 0;
    } //schemaIdInvalid

    /**
     * Returns whether the specified type ID is invalid.
     *
     * @param typeId the type ID to be used in the operation
     * @return {@code true}, if the specified type ID is invalid and {@code false} otherwise
     */
    private boolean checkTypeId(int typeId) {
        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.selectCount()
                            .from(FieldTypes.FIELD_TYPES)
                            .where(FieldTypes.FIELD_TYPES.ID.eq(typeId))
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError().withThrowable(e).log();

            return true;
        } //end try catch

        if (record == null) {
            return true;
        } //end if

        int count = record.get(0, Integer.class);

        return count == 0;
    } //checkTypeId

    /**
     * Attempts to create a new field for the current logged-in user. A schema ID, name, display name, and type ID are
     * required for creation.
     *
     * @param schemaId the schema ID to be used in the operation
     * @param name the name to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam("schema_id") int schemaId, @RequestParam String name,
                                          @RequestParam("display_name") String displayName,
                                          @RequestParam("type_id") int typeId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        if (this.checkSchema(schemaId, creatorId)) {
            String content = "The schema ID %d is not associated with the current logged-in user".formatted(schemaId);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if (name.length() > MAX_NAME_LENGTH) {
            String content = "A name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if (displayName.length() > MAX_NAME_LENGTH) {
            String content = "A display name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }  else if (this.checkTypeId(typeId)) {
            String content = "%d is not a valid type ID".formatted(typeId);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.insertInto(FIELDS)
                            .columns(FIELDS.SCHEMA_ID, FIELDS.NAME, FIELDS.DISPLAY_NAME, FIELDS.TYPE_ID)
                            .values(schemaId, name, displayName, typeId)
                            .returning(FIELDS.ID)
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A field with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (record == null) {
            String content = "A field with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        String content = "A field with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        int id = record.get(FIELDS.ID);

        String locationString = "http://localhost:8080/api/fields?id=%d".formatted(id);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.CREATED);
    } //create

    /**
     * Attempts to read the field data of the current logged-in user. Assuming data exists, the ID, creator ID, schema
     * ID, name, display name, and type ID of each field are returned.
     *
     * @param id the ID to be used in the operation
     * @param name the name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @param displayName the display name to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(name = "schema_id", required = false) Integer schemaId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(name = "display_name", required = false) String displayName,
                                        @RequestParam(name = "type_id", required = false) Integer typeId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        Condition condition = SCHEMAS.CREATOR_ID.eq(creatorId);

        if (id != null) {
            condition = condition.and(FIELDS.ID.eq(id));
        } //end if

        if (schemaId != null) {
            condition = condition.and(FIELDS.SCHEMA_ID.eq(schemaId));
        } //end if

        if (name != null) {
            condition = condition.and(FIELDS.NAME.eq(name));
        } //end if

        if (displayName != null) {
            condition = condition.and(FIELDS.DISPLAY_NAME.eq(displayName));
        } //end if

        if (typeId != null) {
            condition = condition.and(FIELDS.TYPE_ID.eq(typeId));
        } //end if

        Result<Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            result = context.select(FIELDS.asterisk())
                            .from(SCHEMAS)
                            .innerJoin(FIELDS)
                            .on(SCHEMAS.ID.eq(FIELDS.SCHEMA_ID))
                            .where(condition)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The field's data could not be retrieved";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        Set<Map<String, Object>> content = result.stream()
                                                 .map(Record::intoMap)
                                                 .collect(Collectors.toUnmodifiableSet());

        Body<Set<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the field data of the current logged-in user. A field's schema ID, name, display name, and
     * type ID can be updated. An ID and at least one update are required.
     *
     * @param id the ID to be used in the operation
     * @param schemaId the schema ID to be used in the operation
     * @param name the name to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam int id,
                                          @RequestParam(name = "schema_id", required = false) Integer schemaId,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(name = "display_name", required = false) String displayName,
                                          @RequestParam(name = "type_id", required = false) Integer typeId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        if ((schemaId != null) && this.checkSchema(schemaId, creatorId)) {
            String content = "The schema ID %d is not associated with the current logged-in user".formatted(schemaId);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if ((name != null) && (name.length() > MAX_NAME_LENGTH)) {
            String content = "A name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if ((displayName != null) && (displayName.length() > MAX_NAME_LENGTH)) {
            String content = "A display name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if ((typeId != null) && this.checkTypeId(typeId)) {
            String content = "%d is not a valid type ID".formatted(typeId);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Map<Field<?>, Object> fieldToNewValue = new HashMap<>();

        if (schemaId != null) {
            fieldToNewValue.put(FIELDS.SCHEMA_ID, schemaId);
        } //end if

        if (name != null) {
            fieldToNewValue.put(FIELDS.NAME, name);
        } //end if

        if (displayName != null) {
            fieldToNewValue.put(FIELDS.DISPLAY_NAME, displayName);
        } //end if

        if (typeId != null) {
            fieldToNewValue.put(FIELDS.TYPE_ID, typeId);
        } //end if

        if (fieldToNewValue.isEmpty()) {
            String content = "At lease one update is required";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int userId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.update(FIELDS)
                                 .set(fieldToNewValue)
                                 .where(FIELDS.ID.eq(id))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The field's data could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "The field's data could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "The field's data was successfully updated";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the field data of the current logged-in user. An ID is required for deletion.
     *
     * @param id the ID to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the delete operation
     */
    @DeleteMapping
    public ResponseEntity<Body<?>> delete(@RequestParam int id) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int creatorId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.delete(FIELDS)
                                 .where(FIELDS.ID.eq(id))
                                 //.and(FIELDS.CREATOR_ID.eq(creatorId))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The field's data could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "The field's data could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "The field's data was successfully deleted";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //delete
}