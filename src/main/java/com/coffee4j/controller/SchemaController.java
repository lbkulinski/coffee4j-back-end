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
import schema.generated.tables.Schemas;

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
 * The REST controller used to interact with the Coffee4j schema data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 27, 2022
 */
@RestController
@RequestMapping("api/schemas")
public final class SchemaController {
    /**
     * The {@code schemas} table of the {@link SchemaController} class.
     */
    private static final Schemas SCHEMAS;

    /**
     * The maximum name length of the {@link SchemaController} class.
     */
    private static final int MAX_NAME_LENGTH;

    /**
     * The {@link Logger} of the {@link SchemaController} class.
     */
    private static final Logger LOGGER;

    static {
        SCHEMAS = Schemas.SCHEMAS;

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

        int ownerId = user.id();

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            if (defaultFlag) {
                context.update(SCHEMAS)
                       .set(SCHEMAS.DEFAULT, false)
                       .where(SCHEMAS.OWNER_ID.eq(ownerId))
                       .execute();
            } //end if

            record = context.insertInto(SCHEMAS)
                            .columns(SCHEMAS.OWNER_ID, SCHEMAS.NAME, SCHEMAS.DEFAULT, SCHEMAS.SHARED)
                            .values(ownerId, name, defaultFlag, sharedFlag)
                            .returningResult(SCHEMAS.ID)
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A schema with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (record == null) {
            String content = "A schema with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A schema with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        int recordId = record.get(SCHEMAS.ID);

        String locationString = "http://localhost:8080/api/schemas?id=%d".formatted(recordId);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.CREATED);
    } //create

    /**
     * Attempts to read the schema data of the current logged-in user. Assuming data exists, the ID, owner ID, name,
     * default flag, and shared flag of each schema are returned.
     *
     * @param id the ID to be used in the operation
     * @param ownerId the owner ID to be used in the operation
     * @param name the name to be used in the operation
     * @param defaultFlag the default flag to be used in the operation
     * @param sharedFlag the shared flag to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(name = "owner_id", required = false) Integer ownerId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(name = "default", required = false) Boolean defaultFlag,
                                        @RequestParam(name = "shared", required = false) Boolean sharedFlag) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Condition condition = DSL.noCondition();

        if (Objects.equals(sharedFlag, Boolean.TRUE)) {
            condition = condition.and(SCHEMAS.SHARED.isTrue());
        } else {
            if (Objects.equals(sharedFlag, Boolean.FALSE)) {
                condition = condition.and(SCHEMAS.SHARED.isFalse());
            } //end if

            int userId = user.id();

            condition = condition.and(SCHEMAS.OWNER_ID.eq(userId));
        } //end if

        if (id != null) {
            condition = condition.and(SCHEMAS.ID.eq(id));
        } //end if

        if (ownerId != null) {
            condition = condition.and(SCHEMAS.OWNER_ID.eq(ownerId));
        } //end if

        if (name != null) {
            condition = condition.and(SCHEMAS.NAME.eq(name));
        } //end if

        if (defaultFlag != null) {
            condition = condition.and(SCHEMAS.DEFAULT.eq(defaultFlag));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            result = context.select()
                            .from(SCHEMAS)
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

        Set<Map<String, Object>> content = result.stream()
                                                 .map(Record::intoMap)
                                                 .collect(Collectors.toUnmodifiableSet());

        Body<Set<Map<String, Object>>> body = Body.success(content);

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
            fieldToNewValue.put(SCHEMAS.NAME, name);
        } //end if

        if (defaultFlag != null) {
            fieldToNewValue.put(SCHEMAS.DEFAULT, defaultFlag);
        } //end if

        if (sharedFlag != null) {
            fieldToNewValue.put(SCHEMAS.SHARED, sharedFlag);
        } //end if

        if (fieldToNewValue.isEmpty()) {
            String content = "At lease one update is required";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int ownerId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.update(SCHEMAS)
                                 .set(fieldToNewValue)
                                 .where(SCHEMAS.ID.eq(id))
                                 .and(SCHEMAS.OWNER_ID.eq(ownerId))
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

        int ownerId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.delete(SCHEMAS)
                                 .where(SCHEMAS.ID.eq(id))
                                 .and(SCHEMAS.OWNER_ID.eq(ownerId))
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