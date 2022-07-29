/*
 * MIT License
 *
 * Copyright (c) 2022 Logan Kulinski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import schema.generated.tables.Coffee;
import schema.generated.tables.records.CoffeeRecord;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The REST controller used to interact with the Coffee4j coffee data.
 *
 * @author Logan Kulinski, rashes_lineage02@icloud.com
 * @version July 28, 2022
 */
@RestController
@RequestMapping("/api/coffee")
public final class CoffeeController {
    /**
     * The maximum name length of the {@link CoffeeController} class.
     */
    private static final int MAX_NAME_LENGTH;

    /**
     * The {@code coffee} table of the {@link CoffeeController} class.
     */
    private static final Coffee COFFEE;

    /**
     * The {@link Logger} of the {@link CoffeeController} class.
     */
    private static final Logger LOGGER;

    static {
        MAX_NAME_LENGTH = 45;

        COFFEE = Coffee.COFFEE;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to create a new coffee. A name is required for creation.
     *
     * @param name the name to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam String name) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        if (name.length() > MAX_NAME_LENGTH) {
            String content = "The specified name must not be greater than %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int userId = user.id();

        CoffeeRecord record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            record = context.insertInto(COFFEE)
                            .set(COFFEE.USER_ID, userId)
                            .set(COFFEE.NAME, name)
                            .returning(COFFEE.ID)
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A coffee with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (record == null) {
            String content = "A coffee with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A coffee with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        int id = record.getId();

        String locationString = "http://localhost:8080/api/coffee?id=%d".formatted(id);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    } //create

    /**
     * Attempts to read the coffee data of the current logged-in user using the specified offset ID and limit. An ID or
     * name can be used to filter the data. Assuming data exists, the ID and name of each coffee are returned.
     *
     * @param id the ID to be used in the operation
     * @param name the name to be used in the operation
     * @param offsetId the offset ID to be used in the operation
     * @param limit the limit to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) Integer offsetId,
                                        @RequestParam(defaultValue = "10") int limit) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = DSL.noCondition();

        if (offsetId != null) {
            condition = condition.and(COFFEE.ID.lessThan(offsetId));
        } //end if

        condition = condition.and(COFFEE.USER_ID.eq(userId));

        if (id != null) {
            condition = condition.and(COFFEE.ID.eq(id));
        } //end if

        if (name != null) {
            condition = condition.and(COFFEE.NAME.eq(name));
        } //end if

        Result<? extends Record> result;

        int rowCount;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(COFFEE.ID, COFFEE.NAME)
                            .from(COFFEE)
                            .where(condition)
                            .orderBy(COFFEE.ID.desc())
                            .limit(limit)
                            .fetch();

            rowCount = context.fetchCount(COFFEE, COFFEE.USER_ID.eq(userId));
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A coffee with the specified parameters could not be read";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        HttpHeaders httpHeaders = new HttpHeaders();

        String recordCount = String.valueOf(rowCount);

        httpHeaders.add("X-Record-Count", recordCount);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the coffee data of the current logged-in user. A coffee's name can be updated. An ID and name
     * are required for updating.
     *
     * @param id the ID to be used in the operation
     * @param name the name to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam int id, @RequestParam String name) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        if (name.length() > MAX_NAME_LENGTH) {
            String content = "The specified name must not be greater than %d characters".formatted(MAX_NAME_LENGTH);

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int userId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            rowsChanged = context.update(COFFEE)
                                 .set(COFFEE.NAME, name)
                                 .where(COFFEE.ID.eq(id))
                                 .and(COFFEE.USER_ID.eq(userId))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A coffee with the specified parameters could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A coffee with the specified parameters could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A coffee with the specified parameters was successfully updated";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the coffee data of the current logged-in user. A single coffee can be deleted. An ID is
     * required for deletion.
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

        int userId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            rowsChanged = context.deleteFrom(COFFEE)
                                 .where(COFFEE.ID.eq(id))
                                 .and(COFFEE.USER_ID.eq(userId))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A coffee with the specified parameters could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A coffee with the specified parameters could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A coffee with the specified parameters was successfully deleted";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //delete
}