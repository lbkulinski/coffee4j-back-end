package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schema.generated.tables.Brewer;
import schema.generated.tables.records.BrewerRecord;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brewer")
public final class BrewerController {
    private static final Brewer BREWER;

    private static final Logger LOGGER;

    static {
        BREWER = Brewer.BREWER;

        LOGGER = LogManager.getLogger();
    } //static

    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam String name) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        BrewerRecord record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            record = context.insertInto(BREWER)
                            .columns(BREWER.USER_ID, BREWER.NAME)
                            .values(userId, name)
                            .returning(BREWER.ID)
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brewer with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (record == null) {
            String content = "A brewer with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A brewer with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        int id = record.getId();

        String locationString = "http://localhost:8080/api/brewer?id=%d".formatted(id);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    } //create

    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(required = false) String name) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = BREWER.USER_ID.eq(userId);

        if (id != null) {
            condition = condition.and(BREWER.ID.eq(id));
        } //end if

        if (name != null) {
            condition = condition.and(BREWER.NAME.eq(name));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(BREWER.ID, BREWER.NAME)
                            .from(BREWER)
                            .where(condition)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brewer with the specified parameters could not be read";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //read

    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam int id, @RequestParam String name) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            rowsChanged = context.update(BREWER)
                                 .set(BREWER.NAME, name)
                                 .where(BREWER.USER_ID.eq(userId))
                                 .and(BREWER.ID.eq(id))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brewer with the specified parameters could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A brewer with the specified parameters could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A brewer with the specified parameters was successfully updated";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //update

    @DeleteMapping
    public ResponseEntity<Body<?>> delete(@RequestParam int id) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            rowsChanged = context.deleteFrom(BREWER)
                                 .where(BREWER.USER_ID.eq(userId))
                                 .and(BREWER.ID.eq(id))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brewer with the specified parameters could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A brewer with the specified parameters could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A brewer with the specified parameters was successfully deleted";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //delete
}