package com.coffee4j.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import com.coffee4j.Body;
import org.springframework.web.bind.annotation.RequestParam;
import com.coffee4j.security.User;
import com.coffee4j.Utilities;
import org.springframework.http.HttpStatus;
import org.jooq.Table;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.Field;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import java.sql.Connection;
import java.sql.DriverManager;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import java.sql.SQLException;
import org.jooq.exception.DataAccessException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.jooq.Condition;
import java.util.Objects;
import org.jooq.Result;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.HashMap;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * The REST controller used to interact with the Coffee4j schema data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 24, 2022
 */
@RestController
@RequestMapping("api/schemas")
public final class SchemaController {
    /**
     * The maximum name length of the {@link SchemaController} class.
     */
    private static final int MAX_NAME_LENGTH;

    /**
     * The {@link Logger} of the {@link SchemaController} class.
     */
    private static final Logger LOGGER;

    static {
        MAX_NAME_LENGTH = 45;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to create a new schema for the current logged-in user. A name, default flag, and shared flag are
     * required for creation.
     *
     * @param name the name to be used in the operation
     * @param defaultFlag the default flag to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam String name, @RequestParam("default") boolean defaultFlag,
                                          @RequestParam("shared") boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        if (name.length() > MAX_NAME_LENGTH) {
            String content = "A name cannot exceed %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        Table<Record> schemasTable = DSL.table("`schemas`");

        Field<Boolean> defaultField = DSL.field("`default`", Boolean.class);

        Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

        int creatorId = user.id();

        Field<String> nameField = DSL.field("`name`", String.class);

        Field<Boolean> sharedField = DSL.field("`shared`", Boolean.class);

        DataType<Integer> idType = SQLDataType.INTEGER.identity(true);

        Field<Integer> idField = DSL.field("`id`", idType);

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            if (defaultFlag) {
                context.update(schemasTable)
                       .set(defaultField, false)
                       .where(creatorIdField.eq(creatorId))
                       .execute();
            } //end if

            record = context.insertInto(schemasTable)
                            .columns(creatorIdField, nameField, defaultField, sharedField)
                            .values(creatorId, name, defaultFlag, sharedFlag)
                            .returning(idField)
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A schema with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end try catch

        if (record == null) {
            String content = "A schema with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int recordId = record.getValue(idField, Integer.class);

        String locationString = "http://localhost:8080/api/schemas?id=%d".formatted(recordId);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        String content = "A schema with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.CREATED);
    } //create

    /**
     * Attempts to read the schema data of the current logged-in user. Assuming data exists, the ID, creator ID, name,
     * default flag, and shared flag of each schema are returned.
     *
     * @param id the ID to be used in the operation
     * @param creatorId the creator ID to be used in the operation
     * @param name the name to be used in the operation
     * @param defaultFlag the default flag to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(required = false) Integer creatorId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(name = "default", required = false) Boolean defaultFlag,
                                        @RequestParam(name = "shared", required = false) Boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Condition condition = DSL.noCondition();

        Field<Boolean> sharedField = DSL.field("`shared`", Boolean.class);

        Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

        if (Objects.equals(sharedFlag, Boolean.TRUE)) {
            condition = condition.and(sharedField.isTrue());
        } else {
            if (Objects.equals(sharedFlag, Boolean.FALSE)) {
                condition = condition.and(sharedField.isFalse());
            } //end if

            int userId = user.id();

            condition = condition.and(creatorIdField.eq(userId));
        } //end if

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        if (id != null) {
            condition = condition.and(idField.eq(id));
        } //end if

        if (creatorId != null) {
            condition = condition.and(creatorIdField.eq(creatorId));
        } //end if

        Field<String> nameField = DSL.field("`name`", String.class);

        if (name != null) {
            condition = condition.and(nameField.eq(name));
        } //end if

        Field<Boolean> defaultField = DSL.field("`default`", Boolean.class);

        if (defaultFlag != null) {
            condition = condition.and(defaultField.eq(defaultFlag));
        } //end if

        Table<Record> schemasTable = DSL.table("`schemas`");

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            result = context.select(idField, creatorIdField, nameField, defaultField, sharedField)
                            .from(schemasTable)
                            .where(condition)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The schema's data could not be retrieved";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        Set<Map<String, ?>> content = new HashSet<>();

        for (Record record : result) {
            int recordId = record.getValue(idField);

            int recordCreatorId = record.getValue(creatorIdField);

            String recordName = record.getValue(nameField);

            boolean recordDefault = record.getValue(defaultField);

            boolean recordShared = record.getValue(sharedField);

            Map<String, ?> schema = Map.of(
                "id", recordId,
                "creatorId", recordCreatorId,
                "name", recordName,
                "default", recordDefault,
                "shared", recordShared
            );

            content.add(schema);
        } //end for

        Body<Set<Map<String, ?>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the schema data of the current logged-in user. A schema's name, default flag, and shared flag
     * can be updated. An ID and at least one update are required.
     *
     * @param id the ID to be used in the operation
     * @param name the name to be used in the operation
     * @param defaultFlag the default flag to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam int id,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(name = "default", required = false) Boolean defaultFlag,
                                          @RequestParam(name = "shared", required = false) Boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Map<Field<?>, Object> fieldToNewValue = new HashMap<>();

        if (name != null) {
            Field<String> nameField = DSL.field("`name`", String.class);

            fieldToNewValue.put(nameField, name);
        } //end if

        if (defaultFlag != null) {
            Field<Boolean> defaultField = DSL.field("`default`", Boolean.class);

            fieldToNewValue.put(defaultField, defaultFlag);
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

        Table<Record> schemasTable = DSL.table("`schemas`");

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

        int creatorId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.update(schemasTable)
                                 .set(fieldToNewValue)
                                 .where(idField.eq(id))
                                 .and(creatorIdField.eq(creatorId))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The schema's data could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "The schema's data could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "The schema's data was successfully updated";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the schema data of the current logged-in user. An ID is required for deletion.
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

        Table<Record> schemasTable = DSL.table("`schemas`");

        Field<Integer> idField = DSL.field("`id`", Integer.class);

        Field<Integer> creatorIdField = DSL.field("`creator_id`", Integer.class);

        int creatorId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.delete(schemasTable)
                                 .where(idField.eq(id))
                                 .and(creatorIdField.eq(creatorId))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The schema's data could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "The schema's data could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "The schema's data was successfully deleted";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //delete
}