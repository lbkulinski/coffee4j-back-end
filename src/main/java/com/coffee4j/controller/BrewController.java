package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import schema.generated.tables.Brew;
import schema.generated.tables.records.BrewRecord;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

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
                                          @RequestParam("coffee_mass") float coffeeMass,
                                          @RequestParam("water_mass") float waterMass) {
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
}