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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import schema.generated.tables.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The REST controller used to support typeahead with the Coffee4j data.
 *
 * @author Logan Kulinski, rashes_lineage02@icloud.com
 * @version July 11, 2022
 */
@RestController
@RequestMapping("/api/typeahead")
public final class TypeaheadController {
    /**
     * The {@code coffee} table of the {@link TypeaheadController} class.
     */
    private static final Coffee COFFEE;

    /**
     * The {@code water} table of the {@link TypeaheadController} class.
     */
    private static final Water WATER;

    /**
     * The {@code brewer} table of the {@link TypeaheadController} class.
     */
    private static final Brewer BREWER;

    /**
     * The {@code filter} table of the {@link TypeaheadController} class.
     */
    private static final Filter FILTER;

    /**
     * The {@code vessel} table of the {@link TypeaheadController} class.
     */
    private static final Vessel VESSEL;

    /**
     * The {@link Logger} of the {@link TypeaheadController} class.
     */
    private static final Logger LOGGER;

    static {
        COFFEE = Coffee.COFFEE;

        WATER = Water.WATER;

        BREWER = Brewer.BREWER;

        FILTER = Filter.FILTER;

        VESSEL = Vessel.VESSEL;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to read the coffee data of the current logged-in user using the specified search term and limit.
     * Coffees that start with the specified search term are returned. Assuming data exists, the ID and name of each
     * coffee are returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @param limit the limit to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/coffee")
    public ResponseEntity<Body<?>> searchCoffee(@RequestParam(required = false) String searchTerm,
                                                @RequestParam(defaultValue = "10") int limit) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = COFFEE.USER_ID.eq(userId);

        if (searchTerm != null) {
            condition = condition.and(COFFEE.NAME.startsWithIgnoreCase(searchTerm));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(COFFEE.ID, COFFEE.NAME)
                            .from(COFFEE)
                            .where(condition)
                            .orderBy(COFFEE.ID.desc())
                            .limit(limit)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A coffee with the specified parameters could not be found";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //searchCoffee

    /**
     * Attempts to read the water data of the current logged-in user using the specified search term and limit. Waters
     * that start with the specified search term are returned. Assuming data exists, the ID and name of each water are
     * returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @param limit the limit to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/water")
    public ResponseEntity<Body<?>> searchWater(@RequestParam(required = false) String searchTerm,
                                               @RequestParam(defaultValue = "10") int limit) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = WATER.USER_ID.eq(userId);

        if (searchTerm != null) {
            condition = condition.and(WATER.NAME.startsWithIgnoreCase(searchTerm));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(WATER.ID, WATER.NAME)
                            .from(WATER)
                            .where(condition)
                            .orderBy(WATER.ID.desc())
                            .limit(limit)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A water with the specified parameters could not be found";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //searchWater

    /**
     * Attempts to read the brewer data of the current logged-in user using the specified search term and limit.
     * Brewers that start with the specified search term are returned. Assuming data exists, the ID and name of each
     * brewer are returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @param limit the limit to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/brewer")
    public ResponseEntity<Body<?>> searchBrewer(@RequestParam(required = false) String searchTerm,
                                                @RequestParam(defaultValue = "10") int limit) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = BREWER.USER_ID.eq(userId);

        if (searchTerm != null) {
            condition = condition.and(BREWER.NAME.startsWithIgnoreCase(searchTerm));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(BREWER.ID, BREWER.NAME)
                            .from(BREWER)
                            .where(condition)
                            .orderBy(BREWER.ID.desc())
                            .limit(limit)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brewer with the specified parameters could not be found";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //searchBrewer

    /**
     * Attempts to read the filter data of the current logged-in user using the specified search term and limit.
     * Filters that start with the specified search term are returned. Assuming data exists, the ID and name of each
     * filter are returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @param limit the limit to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/filter")
    public ResponseEntity<Body<?>> searchFilter(@RequestParam(required = false) String searchTerm,
                                                @RequestParam(defaultValue = "10") int limit) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = FILTER.USER_ID.eq(userId);

        if (searchTerm != null) {
            condition = condition.and(FILTER.NAME.startsWithIgnoreCase(searchTerm));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(FILTER.ID, FILTER.NAME)
                            .from(FILTER)
                            .where(condition)
                            .orderBy(FILTER.ID.desc())
                            .limit(limit)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A filter with the specified parameters could not be found";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //searchFilter

    /**
     * Attempts to read the vessel data of the current logged-in user using the specified search term and limit.
     * Vessels that start with the specified search term are returned. Assuming data exists, the ID and name of each
     * vessel are returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @param limit the limit to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/vessel")
    public ResponseEntity<Body<?>> searchVessel(@RequestParam(required = false) String searchTerm,
                                                @RequestParam(defaultValue = "10") int limit) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = VESSEL.USER_ID.eq(userId);

        if (searchTerm != null) {
            condition = condition.and(VESSEL.NAME.startsWithIgnoreCase(searchTerm));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(VESSEL.ID, VESSEL.NAME)
                            .from(VESSEL)
                            .where(condition)
                            .orderBy(VESSEL.ID.desc())
                            .limit(limit)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A vessel with the specified parameters could not be found";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //searchVessel
}