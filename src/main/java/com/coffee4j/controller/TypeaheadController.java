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
 * @author Logan Kulinski, lbkulinski@gmail.com
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
     * Attempts to read the coffee data of the current logged-in user using the specified search term. Coffees that
     * start with the specified search term are returned. Assuming data exists, the ID and name of each coffee are
     * returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/coffee")
    public ResponseEntity<Body<?>> searchCoffee(@RequestParam String searchTerm) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = COFFEE.USER_ID.eq(userId);

        condition = condition.and(COFFEE.NAME.startsWithIgnoreCase(searchTerm));

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(COFFEE.ID, COFFEE.NAME)
                            .from(COFFEE)
                            .where(condition)
                            .orderBy(COFFEE.ID.desc())
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
     * Attempts to read the water data of the current logged-in user using the specified search term. Waters that start
     * with the specified search term are returned. Assuming data exists, the ID and name of each water are returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/water")
    public ResponseEntity<Body<?>> searchWater(@RequestParam String searchTerm) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = WATER.USER_ID.eq(userId);

        condition = condition.and(WATER.NAME.startsWithIgnoreCase(searchTerm));

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(WATER.ID, WATER.NAME)
                            .from(WATER)
                            .where(condition)
                            .orderBy(WATER.ID.desc())
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
     * Attempts to read the brewer data of the current logged-in user using the specified search term. Brewers that
     * start with the specified search term are returned. Assuming data exists, the ID and name of each brewer are
     * returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/brewer")
    public ResponseEntity<Body<?>> searchBrewer(@RequestParam String searchTerm) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = BREWER.USER_ID.eq(userId);

        condition = condition.and(BREWER.NAME.startsWithIgnoreCase(searchTerm));

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(BREWER.ID, BREWER.NAME)
                            .from(BREWER)
                            .where(condition)
                            .orderBy(BREWER.ID.desc())
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
     * Attempts to read the filter data of the current logged-in user using the specified search term. Filters that
     * start with the specified search term are returned. Assuming data exists, the ID and name of each filter are
     * returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/filter")
    public ResponseEntity<Body<?>> searchFilter(@RequestParam String searchTerm) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = FILTER.USER_ID.eq(userId);

        condition = condition.and(FILTER.NAME.startsWithIgnoreCase(searchTerm));

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(FILTER.ID, FILTER.NAME)
                            .from(FILTER)
                            .where(condition)
                            .orderBy(FILTER.ID.desc())
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
     * Attempts to read the vessel data of the current logged-in user using the specified search term. Vessels that
     * start with the specified search term are returned. Assuming data exists, the ID and name of each vessel are
     * returned.
     *
     * @param searchTerm the search term to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the search operation
     */
    @GetMapping("/vessel")
    public ResponseEntity<Body<?>> searchVessel(@RequestParam String searchTerm) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = VESSEL.USER_ID.eq(userId);

        condition = condition.and(VESSEL.NAME.startsWithIgnoreCase(searchTerm));

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(VESSEL.ID, VESSEL.NAME)
                            .from(VESSEL)
                            .where(condition)
                            .orderBy(VESSEL.ID.desc())
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