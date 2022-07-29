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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The REST controller used to interact with the Coffee4j brew data.
 *
 * @author Logan Kulinski, rashes_lineage02@icloud.com
 * @version July 28, 2022
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
     * Attempts to create a new brew. A coffee ID, water ID, brewer ID, filter ID, vessel ID, coffee mass, and water
     * mass are required for creation.
     *
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
    public ResponseEntity<Body<?>> create(@RequestParam int coffeeId, @RequestParam int waterId,
                                          @RequestParam int brewerId, @RequestParam int filterId,
                                          @RequestParam int vesselId, @RequestParam BigDecimal coffeeMass,
                                          @RequestParam BigDecimal waterMass) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
        
        BrewRecord record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            record = context.insertInto(BREW)
                            .set(BREW.USER_ID, userId)
                            .set(BREW.TIMESTAMP, timestamp)
                            .set(BREW.COFFEE_ID, coffeeId)
                            .set(BREW.WATER_ID, waterId)
                            .set(BREW.BREWER_ID, brewerId)
                            .set(BREW.FILTER_ID, filterId)
                            .set(BREW.VESSEL_ID, vesselId)
                            .set(BREW.COFFEE_MASS, coffeeMass)
                            .set(BREW.WATER_MASS, waterMass)
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
     * Returns a brew using the specified record.
     *
     * @param record the record to be used in the operation
     * @return a brew using the specified record
     * @throws NullPointerException if the specified record is {@code null}
     */
    private Map<String, Object> getBrew(Record record) {
        Objects.requireNonNull(record, "the specified record is null");

        int id = record.get(BREW.ID);

        LocalDateTime timestamp = record.get(BREW.TIMESTAMP);

        int coffeeId = record.get(COFFEE.ID);

        String coffeeName = record.get(COFFEE.NAME);

        int waterId = record.get(WATER.ID);

        String waterName = record.get(WATER.NAME);

        int brewerId = record.get(BREWER.ID);

        String brewerName = record.get(BREWER.NAME);

        int filterId = record.get(FILTER.ID);

        String filterName = record.get(FILTER.NAME);

        int vesselId = record.get(VESSEL.ID);

        String vesselName = record.get(VESSEL.NAME);

        BigDecimal coffeeMass = record.get(BREW.COFFEE_MASS);

        BigDecimal waterMass = record.get(BREW.WATER_MASS);

        return Map.of(
            "id", id,
            "timestamp", timestamp,
            "coffee", Map.of(
                "id", coffeeId,
                "name", coffeeName
            ),
            "water", Map.of(
                "id", waterId,
                "name", waterName
            ),
            "brewer", Map.of(
                "id", brewerId,
                "name", brewerName
            ),
            "filter", Map.of(
                "id", filterId,
                "name", filterName
            ),
            "vessel", Map.of(
                "id", vesselId,
                "name", vesselName
            ),
            "coffeeMass", coffeeMass,
            "waterMass", waterMass
        );
    } //getBrew

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
                                        @RequestParam(required = false) String timestampString,
                                        @RequestParam(required = false) Integer coffeeId,
                                        @RequestParam(required = false) Integer waterId,
                                        @RequestParam(required = false) Integer brewerId,
                                        @RequestParam(required = false) Integer filterId,
                                        @RequestParam(required = false) Integer vesselId,
                                        @RequestParam(required = false) BigDecimal coffeeMass,
                                        @RequestParam(required = false) BigDecimal waterMass,
                                        @RequestParam(required = false) Integer offsetId,
                                        @RequestParam(defaultValue = "10") int limit) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int userId = user.id();

        Condition condition = DSL.noCondition();

        if (offsetId != null) {
            condition = condition.and(BREW.ID.lessThan(offsetId));
        } //end if

        condition = condition.and(BREW.USER_ID.eq(userId));

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
            condition = condition.and(COFFEE.ID.eq(coffeeId));
        } //end if

        if (waterId != null) {
            condition = condition.and(WATER.ID.eq(waterId));
        } //end if

        if (brewerId != null) {
            condition = condition.and(BREWER.ID.eq(brewerId));
        } //end if

        if (filterId != null) {
            condition = condition.and(FILTER.ID.eq(filterId));
        } //end if

        if (vesselId != null) {
            condition = condition.and(VESSEL.ID.eq(vesselId));
        } //end if

        if (coffeeMass != null) {
            condition = condition.and(BREW.COFFEE_MASS.eq(coffeeMass));
        } //end if

        if (waterMass != null) {
            condition = condition.and(BREW.WATER_MASS.eq(waterMass));
        } //end if

        Result<? extends Record> result;

        int rowCount;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select(BREW.ID, BREW.TIMESTAMP, COFFEE.ID, COFFEE.NAME, WATER.ID, WATER.NAME, BREWER.ID,
                                    BREWER.NAME, FILTER.ID, FILTER.NAME, VESSEL.ID, VESSEL.NAME, BREW.COFFEE_MASS,
                                    BREW.WATER_MASS)
                            .from(BREW)
                            .join(COFFEE)
                            .on(COFFEE.ID.eq(BREW.COFFEE_ID))
                            .join(WATER)
                            .on(WATER.ID.eq(BREW.WATER_ID))
                            .join(BREWER)
                            .on(BREWER.ID.eq(BREW.BREWER_ID))
                            .join(FILTER)
                            .on(FILTER.ID.eq(BREW.FILTER_ID))
                            .join(VESSEL)
                            .on(VESSEL.ID.eq(BREW.VESSEL_ID))
                            .where(condition)
                            .orderBy(BREW.ID.desc())
                            .limit(limit)
                            .fetch();

            rowCount = context.fetchCount(BREW, BREW.USER_ID.eq(userId));
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A brew with the specified parameters could not be read";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        List<Map<String, Object>> content = result.stream()
                                                  .map(this::getBrew)
                                                  .toList();

        Body<List<Map<String, Object>>> body = Body.success(content);

        HttpHeaders httpHeaders = new HttpHeaders();

        String recordCount = String.valueOf(rowCount);

        httpHeaders.add("X-Record-Count", recordCount);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
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
                                          @RequestParam(name = "timestamp", required = false) String timestampString,
                                          @RequestParam(required = false) Integer coffeeId,
                                          @RequestParam(required = false) Integer waterId,
                                          @RequestParam(required = false) Integer brewerId,
                                          @RequestParam(required = false) Integer filterId,
                                          @RequestParam(required = false) Integer vesselId,
                                          @RequestParam(required = false) BigDecimal coffeeMass,
                                          @RequestParam(required = false) BigDecimal waterMass) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        Map<Field<?>, Object> fieldToNewValue = new HashMap<>();

        if (timestampString != null) {
            Instant instant;

            try {
                instant = Instant.parse(timestampString);
            } catch (DateTimeParseException e) {
                LOGGER.atError()
                      .withThrowable(e)
                      .log();

                String content = "The specified timestamp is malformed";

                Body<String> body = Body.error(content);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
            } //end try catch

            LocalDateTime timestamp = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

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