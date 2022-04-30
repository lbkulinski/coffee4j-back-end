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

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The REST controller used to interact with the Coffee4j field data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 27, 2022
 */
@RestController
@RequestMapping("api/fields")
public final class FieldController {
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
        FIELDS = Fields.FIELDS;

        MAX_NAME_LENGTH = 45;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Returns whether the specified type ID is invalid.
     *
     * @param typeId the type ID to be used in the operation
     * @return {@code true}, if the specified type ID is invalid and {@code false} otherwise
     */
    private boolean checkTypeId(int typeId) {
        boolean typeIdExists;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            typeIdExists = context.fetchExists(FieldTypes.FIELD_TYPES, FieldTypes.FIELD_TYPES.ID.eq(typeId));
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError().withThrowable(e).log();

            return true;
        } //end try catch

        return typeIdExists;
    } //checkTypeId

    /**
     * Attempts to create a new field for the current logged-in user. A name, display name, type ID, and shared flag
     * are required for creation.
     *
     * @param name the name to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @param sharedFlag  the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam String name, @RequestParam("display_name") String displayName,
                                          @RequestParam("type_id") int typeId,
                                          @RequestParam("shared") boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        String errorContent = null;

        if (name.length() > MAX_NAME_LENGTH) {
            errorContent = "A name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);
        } else if (displayName.length() > MAX_NAME_LENGTH) {
            errorContent = "A display name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);
        }  else if (this.checkTypeId(typeId)) {
            errorContent = "%d is not a valid type ID".formatted(typeId);
        } //end if

        if (errorContent != null) {
            Body<String> body = Body.error(errorContent);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int ownerId = user.id();

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.insertInto(FIELDS)
                            .columns(FIELDS.OWNER_ID, FIELDS.NAME, FIELDS.DISPLAY_NAME, FIELDS.TYPE_ID, FIELDS.SHARED)
                            .values(ownerId, name, displayName, typeId, sharedFlag)
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
     * Attempts to read the field data of the current logged-in user. Assuming data exists, the ID, owner ID, name,
     * display name, type ID, and shared flag of each field are returned.
     *
     * @param id the ID to be used in the operation
     * @param ownerId the owner ID to be used in the operation
     * @param name the name to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(name = "owner_id", required = false) Integer ownerId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(name = "display_name", required = false) String displayName,
                                        @RequestParam(name = "type_id", required = false) Integer typeId,
                                        @RequestParam(name = "shared", required = false) Boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Condition condition = DSL.noCondition();

        if (Objects.equals(sharedFlag, Boolean.TRUE)) {
            condition = condition.and(FIELDS.SHARED.isTrue());
        } else {
            if (Objects.equals(sharedFlag, Boolean.FALSE)) {
                condition = condition.and(FIELDS.SHARED.isFalse());
            } //end if

            int userId = user.id();

            condition = condition.and(FIELDS.OWNER_ID.eq(userId));
        } //end if

        if (id != null) {
            condition = condition.and(FIELDS.ID.eq(id));
        } //end if

        if (ownerId != null) {
            condition = condition.and(FIELDS.OWNER_ID.eq(ownerId));
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

            result = context.select()
                            .from(FIELDS)
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
     * Attempts to update the field data of the current logged-in user. A field's owner ID, name, display name, type
     * ID, and shared flag can be updated. An ID and at least one update are required.
     *
     * @param id the ID to be used in the operation
     * @param ownerId the owner ID to be used in the operation
     * @param name the name to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam int id,
                                          @RequestParam(name = "owner_id", required = false) Integer ownerId,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(name = "display_name", required = false) String displayName,
                                          @RequestParam(name = "type_id", required = false) Integer typeId,
                                          @RequestParam(name = "shared", required = false) Boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        String errorContent = null;

        if ((name != null) && (name.length() > MAX_NAME_LENGTH)) {
            errorContent = "A name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);
        } else if ((displayName != null) && (displayName.length() > MAX_NAME_LENGTH)) {
            errorContent = "A display name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);
        } else if ((typeId != null) && this.checkTypeId(typeId)) {
            errorContent = "%d is not a valid type ID".formatted(typeId);
        } //end if

        if (errorContent != null) {
            Body<String> body = Body.error(errorContent);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Map<Field<?>, Object> fieldToNewValue = new HashMap<>();

        if (ownerId != null) {
            fieldToNewValue.put(FIELDS.OWNER_ID, ownerId);
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

        if (sharedFlag != null) {
            fieldToNewValue.put(FIELDS.SHARED, sharedFlag);
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
                                 .and(FIELDS.OWNER_ID.eq(userId))
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

        int ownerId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.delete(FIELDS)
                                 .where(FIELDS.ID.eq(id))
                                 .and(FIELDS.OWNER_ID.eq(ownerId))
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