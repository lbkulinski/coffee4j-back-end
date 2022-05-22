package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schema.generated.tables.Brew;
import schema.generated.tables.records.BrewRecord;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brew")
public final class BrewController {
    private static final Brew BREW;

    private static final Logger LOGGER;

    static {
        BREW = Brew.BREW;

        LOGGER = LogManager.getLogger();
    } //static

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

        Result<Record> result;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            result = context.select()
                            .from(BREW)
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
}