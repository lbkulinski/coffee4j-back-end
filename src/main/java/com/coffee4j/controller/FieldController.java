package com.coffee4j.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jooq.Table;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import com.coffee4j.Utilities;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import java.sql.SQLException;
import org.jooq.exception.DataAccessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import com.coffee4j.Body;
import org.springframework.web.bind.annotation.RequestParam;
import com.coffee4j.security.User;
import org.springframework.http.HttpStatus;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.jooq.Condition;
import org.jooq.Result;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.HashMap;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * The REST controller used to interact with the Coffee4j field data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 24, 2022
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
     * Returns whether the specified type ID is invalid.
     *
     * @param typeId the type ID to be used in the operation
     * @return {@code true}, if the specified type ID is invalid and {@code false} otherwise
     */
    private boolean typeIdInvalid(int typeId) {
        Table<Record> fieldTypesTable = DSL.table("`field_types`");

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.selectCount()
                            .from(fieldTypesTable)
                            .where(idField.eq(typeId))
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
    } //typeIdInvalid

    /**
     * Attempts to create a new field for the current logged-in user. A name, type ID, display name, and shared flag
     * required for creation.
     *
     * @param name the name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam String name, @RequestParam("type_id") int typeId,
                                          @RequestParam("display_name") String displayName,
                                          @RequestParam("shared") boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        if (name.length() > MAX_NAME_LENGTH) {
            String content = "A name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if (displayName.length() > MAX_NAME_LENGTH) {
            String content = "A display name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if (this.typeIdInvalid(typeId)) {
            String content = "%d is not a valid type ID".formatted(typeId);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Table<Record> fieldsTable = DSL.table("`fields`");

        Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

        Field<String> nameField = DSL.field("`name`", String.class);

        Field<Integer> typeIdField = DSL.field("`type_id`", Integer.class);

        Field<String> displayNameField = DSL.field("`display_name`", String.class);

        Field<Boolean> sharedField = DSL.field("`shared`", Boolean.class);

        int creatorId = user.id();

        DataType<Integer> idType = SQLDataType.INTEGER.identity(true);

        Field<Integer> idField = DSL.field("`id`", idType);

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.insertInto(fieldsTable)
                            .columns(creatorIdField, nameField, typeIdField, displayNameField, sharedField)
                            .values(creatorId, name, typeId, displayName, sharedFlag)
                            .returning(idField)
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

        int id = record.get(idField);

        String locationString = "http://localhost:8080/api/fields?id=%d".formatted(id);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.CREATED);
    } //create

    /**
     * Attempts to read the field data of the current logged-in user. Assuming data exists, the ID, creator ID, name,
     * type ID, display name, and shared flag of each field are returned.
     *
     * @param id the ID to be used in the operation
     * @param creatorId the creator ID to be used in the operation
     * @param name the name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(name = "creator_id", required = false) Integer creatorId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(name = "type_id", required = false) Integer typeId,
                                        @RequestParam(name = "display_name", required = false) String displayName,
                                        @RequestParam(name = "shared", required = false) Boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Condition condition = DSL.noCondition();

        if (sharedFlag == null) {
            Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

            int userId = user.id();

            condition = condition.and(creatorIdField.eq(userId));
        } else if (sharedFlag) {
            Field<Boolean> sharedField = DSL.field("`shared`", Boolean.class);

            condition = condition.and(sharedField.isTrue());
        } else {
            Field<Boolean> sharedField = DSL.field("`shared`", Boolean.class);

            condition = condition.and(sharedField.isFalse());

            Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

            int userId = user.id();

            condition = condition.and(creatorIdField.eq(userId));
        } //end if

        if (id != null) {
            Field<Integer> idField = DSL.field("`id`", Integer.class);

            condition = condition.and(idField.eq(id));
        } //end if

        if (creatorId != null) {
            Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

            condition = condition.and(creatorIdField.eq(creatorId));
        } //end if

        if (name != null) {
            Field<String> nameField = DSL.field("`name`", String.class);

            condition = condition.and(nameField.eq(name));
        } //end if

        if (typeId != null) {
            Field<Integer> typeIdField = DSL.field("`type_id`", Integer.class);

            condition = condition.and(typeIdField.eq(typeId));
        } //end if

        if (displayName != null) {
            Field<String> displayNameField = DSL.field("`display_name`", String.class);

            condition = condition.and(displayNameField.eq(displayName));
        } //end if

        Table<Record> fieldsTable = DSL.table("`fields`");

        Result<Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            result = context.select()
                            .from(fieldsTable)
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
     * Attempts to update the field data of the current logged-in user. A field's name, type ID, display name, and
     * shared flag can be updated. An ID and at least one update are required.
     *
     * @param id the ID to be used in the operation
     * @param name the name to be used in the operation
     * @param typeId the type ID to be used in the operation
     * @param displayName the display name to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam int id, @RequestParam(required = false) String name,
                                          @RequestParam(name = "type_id", required = false) Integer typeId,
                                          @RequestParam(name = "display_name", required = false) String displayName,
                                          @RequestParam(name = "shared", required = false) Boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        if ((name != null) && (name.length() > MAX_NAME_LENGTH)) {
            String content = "A name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if ((displayName != null) && (displayName.length() > MAX_NAME_LENGTH)) {
            String content = "A display name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else if ((typeId != null) && this.typeIdInvalid(typeId)) {
            String content = "%d is not a valid type ID".formatted(typeId);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Map<Field<?>, Object> fieldToNewValue = new HashMap<>();

        if (name != null) {
            Field<String> nameField = DSL.field("`name`", String.class);

            fieldToNewValue.put(nameField, name);
        } //end if

        if (typeId != null) {
            Field<Integer> typeIdField = DSL.field("`type_id`", Integer.class);

            fieldToNewValue.put(typeIdField, typeId);
        } //end if

        if (displayName != null) {
            Field<String> displayNameField = DSL.field("`display_name`", String.class);

            fieldToNewValue.put(displayNameField, displayName);
        } //end if

        if (sharedFlag != null) {
            Field<Boolean> sharedField = DSL.field("`shared`", Boolean.class);

            fieldToNewValue.put(sharedField, sharedFlag);
        } //end if

        if (fieldToNewValue.isEmpty()) {
            String content = "At lease one update is required";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Table<Record> fieldsTable = DSL.table("`fields`");

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

        int creatorId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.update(fieldsTable)
                                 .set(fieldToNewValue)
                                 .where(idField.eq(id))
                                 .and(creatorIdField.eq(creatorId))
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

        Table<Record> fieldsTable = DSL.table("`fields`");

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

        int creatorId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.delete(fieldsTable)
                                 .where(idField.eq(id))
                                 .and(creatorIdField.eq(creatorId))
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