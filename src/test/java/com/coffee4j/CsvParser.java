package com.coffee4j;

import org.jooq.DSLContext;
import org.jooq.Row;
import org.jooq.SQL;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import schema.generated.tables.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.*;

public final class CsvParser {
    public static void main(String[] args) {
        String pathString = "src/test/resources/coffee-log.csv";

        Path path = Path.of(pathString);

        int nextCoffeeId = 1;

        Map<String, Integer> coffeeToId = new LinkedHashMap<>();

        int nextWaterId = 1;

        Map<String, Integer> waterToId = new LinkedHashMap<>();

        int nextBrewerId = 1;

        Map<String, Integer> brewerToId = new LinkedHashMap<>();

        int nextFilterId = 1;

        Map<String, Integer> filterToId = new LinkedHashMap<>();

        int nextVesselId = 1;

        Map<String, Integer> vesselToId = new LinkedHashMap<>();

        List<List<?>> valuesList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine();

            while (line != null) {
                String separator = ",";

                String[] parts = line.split(separator);

                int expectedLength = 9;

                if (parts.length != expectedLength) {
                    continue;
                } //end if

                String coffee = parts[2];

                if (!coffeeToId.containsKey(coffee)) {
                    coffeeToId.put(coffee, nextCoffeeId);

                    nextCoffeeId++;
                } //end if

                String water = parts[3];

                if (!waterToId.containsKey(water)) {
                    waterToId.put(water, nextWaterId);

                    nextWaterId++;
                } //end if

                String brewer = parts[4];

                if (!brewerToId.containsKey(brewer)) {
                    brewerToId.put(brewer, nextBrewerId);

                    nextBrewerId++;
                } //end if

                String filter = parts[5];

                if (!filterToId.containsKey(filter)) {
                    filterToId.put(filter, nextFilterId);

                    nextFilterId++;
                } //end if

                String vessel = parts[6];

                if (!vesselToId.containsKey(vessel)) {
                    vesselToId.put(vessel, nextVesselId);

                    nextVesselId++;
                } //end if

                String date = parts[0];

                String time = parts[1];

                String timestampString = "%s %s".formatted(date, time);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M[M]/d[d]/yy h[h]:m[m] a");

                LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter)
                                                       .atZone(ZoneId.of("America/Chicago"))
                                                       .withZoneSameInstant(ZoneId.of("UTC"))
                                                       .toLocalDateTime();

                int coffeeId = coffeeToId.get(coffee);

                int waterId = waterToId.get(water);

                int brewerId = brewerToId.get(brewer);

                int filterId = filterToId.get(filter);

                int vesselId = vesselToId.get(vessel);

                BigDecimal coffeeMass = new BigDecimal(parts[7]);

                BigDecimal waterMass = new BigDecimal(parts[8]);

                valuesList.add(List.of(1, timestamp, coffeeId, waterId, brewerId, filterId, vesselId, coffeeMass,
                                       waterMass));

                line = reader.readLine();
            } //end while
        } catch (IOException e) {
            e.printStackTrace();

            return;
        } //end try catch

        Set<String> coffees = coffeeToId.keySet();

        Set<String> waters = waterToId.keySet();

        Set<String> brewers = brewerToId.keySet();

        Set<String> filters = filterToId.keySet();

        Set<String> vessels = vesselToId.keySet();

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            for (String coffee : coffees) {
                context.insertInto(Coffee.COFFEE)
                       .columns(Coffee.COFFEE.USER_ID, Coffee.COFFEE.NAME)
                       .values(1, coffee)
                       .execute();
            } //end for

            for (String water : waters) {
                context.insertInto(Water.WATER)
                       .columns(Water.WATER.USER_ID, Water.WATER.NAME)
                       .values(1, water)
                       .execute();
            } //end for

            for (String brewer : brewers) {
                context.insertInto(Brewer.BREWER)
                       .columns(Brewer.BREWER.USER_ID, Brewer.BREWER.NAME)
                       .values(1, brewer)
                       .execute();
            } //end for

            for (String filter : filters) {
                context.insertInto(Filter.FILTER)
                       .columns(Filter.FILTER.USER_ID, Filter.FILTER.NAME)
                       .values(1, filter)
                       .execute();
            } //end for

            for (String vessel : vessels) {
                context.insertInto(Vessel.VESSEL)
                       .columns(Vessel.VESSEL.USER_ID, Vessel.VESSEL.NAME)
                       .values(1, vessel)
                       .execute();
            } //end for

            for (List<?> values : valuesList) {
                context.insertInto(Brew.BREW)
                       .columns(Brew.BREW.USER_ID, Brew.BREW.TIMESTAMP, Brew.BREW.COFFEE_ID, Brew.BREW.WATER_ID,
                                Brew.BREW.BREWER_ID, Brew.BREW.FILTER_ID, Brew.BREW.VESSEL_ID, Brew.BREW.COFFEE_MASS,
                                Brew.BREW.WATER_MASS)
                       .values(values)
                       .execute();
            } //end for
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        } //end try catch
    } //main
}