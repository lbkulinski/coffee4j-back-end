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
import schema.generated.tables.*;
import schema.generated.tables.records.BrewRecord;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The REST controller used to interact with the Coffee4j brew data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version May 27, 2022
 */
@RestController
@RequestMapping("/api/brew")
public final class BrewController {
    /**
     * The {@code brew} table of the {@link BrewController} class.
     */
    private static final Brew BREW;

    /**
     * The {@code coffee} table of the {@link BrewController} class.
     */
    private static final Coffee COFFEE;

    /**
     * The {@code water} table of the {@link BrewController} class.
     */
    private static final Water WATER;

    /**
     * The {@code brewer} table of the {@link BrewController} class.
     */
    private static final Brewer BREWER;

    /**
     * The {@code filter} table of the {@link BrewController} class.
     */
    private static final Filter FILTER;

    /**
     * The {@code vessel} table of the {@link BrewController} class.
     */
    private static final Vessel VESSEL;

    /**
     * The {@link Logger} of the {@link BrewController} class.
     */
    private static final Logger LOGGER;

    static {
        BREW = Brew.BREW;

        COFFEE = Coffee.COFFEE;

        WATER = Water.WATER;

        BREWER = Brewer.BREWER;

        FILTER = Filter.FILTER;

        VESSEL = Vessel.VESSEL;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Attempts to create a new brew. A timestamp, coffee ID, water ID, brewer ID, filter ID, vessel ID, coffee mass,
     * and water mass are required for creation.
     *
     * @param timestampString the timestamp {@link String} to be used in the operation
     * @param coffeeId the coffee ID to be used in the operation
     * @param waterId the water ID to be used in the operation
     * @param brewerId the brewer ID to be used in the operation
     * @param filterId the filter ID to be used in the operation
     * @param vesselId the vessel ID to be used in the operation
     * @param coffeeMass the coffee mass to be used in the operation
     * @param waterMass the water mass to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the create operation
     */
    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam("timestamp") String timestampString,
                                          @RequestParam("coffee_id") int coffeeId,
                                          @RequestParam("water_id") int waterId,
                                          @RequestParam("brewer_id") int brewerId,
                                          @RequestParam("filter_id") int filterId,
                                          @RequestParam("vessel_id") int vesselId,
                                          @RequestParam("coffee_mass") BigDecimal coffeeMass,
                                          @RequestParam("water_mass") BigDecimal waterMass) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        LocalDateTime timestamp;

        try {
            timestamp = LocalDateTime.parse(timestampString);
        } catch (DateTimeParseException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "The specified timestamp is malformed";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end try catch

        int userId = user.id();

        BrewRecord record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            record = context.insertInto(BREW)
                            .columns(BREW.USER_ID, BREW.TIMESTAMP, BREW.COFFEE_ID, BREW.WATER_ID, BREW.BREWER_ID,
                                     BREW.FILTER_ID, BREW.VESSEL_ID, BREW.COFFEE_MASS, BREW.WATER_MASS)
                            .values(userId, timestamp, coffeeId, waterId, brewerId, filterId, vesselId, coffeeMass,
                                    waterMass)
                            .returning(BREW.ID)
                            .fetchOne();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brew with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (record == null) {
            String content = "A brew with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A brew with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        int id = record.getId();

        String locationString = "http://localhost:8080/api/brew?id=%d".formatted(id);

        URI location = URI.create(locationString);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(location);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    } //create

    /**
     * Attempts to read the brew data of the current logged-in user. An ID, timestamp, coffee ID, water ID, brewer ID,
     * filter ID, vessel ID, coffee mass, or water mass can be used to filter the data. Assuming data exists, the ID,
     * timestamp, coffee ID, coffee name, water ID, water name, brewer ID, brew name, filter ID, filter name, vessel
     * ID, vessel name, coffee mass, and water mass of each brew are returned.
     *
     * @param id the ID to be used in the operation
     * @param timestampString the timestamp {@link String} to be used in the operation
     * @param coffeeId the coffee ID to be used in the operation
     * @param waterId the water ID to be used in the operation
     * @param brewerId the brewer ID to be used in the operation
     * @param filterId the filter ID to be used in the operation
     * @param vesselId the vessel ID to be used in the operation
     * @param coffeeMass the coffee mass to be used in the operation
     * @param waterMass the water mass to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the read operation
     */
    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(required = false) Integer id,
                                        @RequestParam(required = false, name = "timestamp") String timestampString,
                                        @RequestParam(required = false, name = "coffee_id") Integer coffeeId,
                                        @RequestParam(required = false, name = "water_id") Integer waterId,
                                        @RequestParam(required = false, name = "brewer_id") Integer brewerId,
                                        @RequestParam(required = false, name = "filter_id") Integer filterId,
                                        @RequestParam(required = false, name = "vessel_id") Integer vesselId,
                                        @RequestParam(required = false, name = "coffee_mass") BigDecimal coffeeMass,
                                        @RequestParam(required = false, name = "water_mass") BigDecimal waterMass) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = BREW.USER_ID.eq(userId);

        if (id != null) {
            condition = BREW.ID.eq(id);
        } //end if

        if (timestampString != null) {
            LocalDateTime timestamp;

            try {
                timestamp = LocalDateTime.parse(timestampString);
            } catch (DateTimeParseException e) {
                LOGGER.atError()
                      .withThrowable(e)
                      .log();

                String content = "The specified timestamp is malformed";

                Body<String> body = Body.error(content);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
            } //end try catch

            condition = condition.and(BREW.TIMESTAMP.eq(timestamp));
        } //end if

        if (coffeeId != null) {
            condition = condition.and(BREW.COFFEE_ID.eq(coffeeId));
        } //end if

        if (waterId != null) {
            condition = condition.and(BREW.WATER_ID.eq(waterId));
        } //end if

        if (brewerId != null) {
            condition = condition.and(BREW.BREWER_ID.eq(brewerId));
        } //end if

        if (filterId != null) {
            condition = condition.and(BREW.FILTER_ID.eq(filterId));
        } //end if

        if (vesselId != null) {
            condition = condition.and(BREW.VESSEL_ID.eq(vesselId));
        } //end if

        if (coffeeMass != null) {
            condition = condition.and(BREW.COFFEE_MASS.eq(coffeeMass));
        } //end if

        if (waterMass != null) {
            condition = condition.and(BREW.WATER_MASS.eq(waterMass));
        } //end if

        Result<? extends Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(BREW.ID, BREW.TIMESTAMP, BREW.COFFEE_ID, COFFEE.NAME.as("coffee_name"),
                                    BREW.WATER_ID, WATER.NAME.as("water_name"), BREW.BREWER_ID,
                                    BREWER.NAME.as("brewer_name"), BREW.FILTER_ID, FILTER.NAME.as("filter_name"),
                                    BREW.VESSEL_ID, VESSEL.NAME.as("vessel_name"))
                            .from(BREW)
                            .join(COFFEE).on(COFFEE.ID.eq(BREW.COFFEE_ID))
                            .join(WATER).on(WATER.ID.eq(BREW.WATER_ID))
                            .join(BREWER).on(BREWER.ID.eq(BREW.BREWER_ID))
                            .join(FILTER).on(FILTER.ID.eq(BREW.FILTER_ID))
                            .join(VESSEL).on(VESSEL.ID.eq(BREW.VESSEL_ID))
                            .where(condition)
                            .fetch();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brew with the specified parameters could not be read";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.intoMaps();

        Body<List<Map<String, Object>>> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //read

    /**
     * Attempts to update the brew data of the current logged-in user. A brew's timestamp, coffee ID, water ID, brewer
     * ID, filter ID, vessel ID, coffee mass, and water mass can be updated. An ID and at least one new value are
     * required for updating.
     *
     * @param id the ID to be used in the operation
     * @param timestampString the timestamp {@link String} to be used in the operation
     * @param coffeeId the coffee ID to be used in the operation
     * @param waterId the water ID to be used in the operation
     * @param brewerId the brewer ID to be used in the operation
     * @param filterId the filter ID to be used in the operation
     * @param vesselId the vessel ID to be used in the operation
     * @param coffeeMass the coffee mass to be used in the operation
     * @param waterMass the water mass to be used in the operation
     * @return a {@link ResponseEntity} containing the outcome of the update operation
     */
    @PutMapping
    public ResponseEntity<Body<?>> update(@RequestParam int id,
                                          @RequestParam(required = false, name = "timestamp") String timestampString,
                                          @RequestParam(required = false, name = "coffee_id") Integer coffeeId,
                                          @RequestParam(required = false, name = "water_id") Integer waterId,
                                          @RequestParam(required = false, name = "brewer_id") Integer brewerId,
                                          @RequestParam(required = false, name = "filter_id") Integer filterId,
                                          @RequestParam(required = false, name = "vessel_id") Integer vesselId,
                                          @RequestParam(required = false, name = "coffee_mass") BigDecimal coffeeMass,
                                          @RequestParam(required = false, name = "water_mass") BigDecimal waterMass) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Map<Field<?>, Object> fieldToNewValue = new HashMap<>();

        if (timestampString != null) {
            LocalDateTime timestamp;

            try {
                timestamp = LocalDateTime.parse(timestampString);
            } catch (DateTimeParseException e) {
                LOGGER.atError()
                      .withThrowable(e)
                      .log();

                String content = "The specified timestamp is malformed";

                Body<String> body = Body.error(content);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
            } //end try catch

            fieldToNewValue.put(BREW.TIMESTAMP, timestamp);
        } //end if

        if (coffeeId != null) {
            fieldToNewValue.put(BREW.COFFEE_ID, coffeeId);
        } //end if

        if (waterId != null) {
            fieldToNewValue.put(BREW.WATER_ID, waterId);
        } //end if

        if (brewerId != null) {
            fieldToNewValue.put(BREW.BREWER_ID, brewerId);
        } //end if

        if (filterId != null) {
            fieldToNewValue.put(BREW.FILTER_ID, filterId);
        } //end if

        if (vesselId != null) {
            fieldToNewValue.put(BREW.VESSEL_ID, vesselId);
        } //end if

        if (coffeeMass != null) {
            fieldToNewValue.put(BREW.COFFEE_MASS, coffeeMass);
        } //end if

        if (waterMass != null) {
            fieldToNewValue.put(BREW.WATER_MASS, waterMass);
        } //end if

        if (fieldToNewValue.isEmpty()) {
            String content = "At least one value must be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        int userId = user.id();

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            rowsChanged = context.update(BREW)
                                 .set(fieldToNewValue)
                                 .where(BREW.ID.eq(id))
                                 .and(BREW.USER_ID.eq(userId))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brew with the specified parameters could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A brew with the specified parameters could not be updated";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A brew with the specified parameters was successfully updated";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete the brew data of the current logged-in user. A single brew can be deleted. An ID is required
     * for deletion.
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

            rowsChanged = context.deleteFrom(BREW)
                                 .where(BREW.ID.eq(id))
                                 .and(BREW.USER_ID.eq(userId))
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brew with the specified parameters could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "A brew with the specified parameters could not be deleted";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "A brew with the specified parameters was successfully deleted";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //delete
}