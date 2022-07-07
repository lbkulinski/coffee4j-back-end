package com.coffee4j;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import schema.generated.tables.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class DataFiller {
    private static void createRandomBrew() {
        int userId = 1;

        LocalDateTime timestamp = ZonedDateTime.now(ZoneId.of("UTC"))
                                               .toLocalDateTime();

        RandomGenerator generator = RandomGenerator.getDefault();

        int coffeeId = generator.nextInt(1, 100_001);

        int waterId = generator.nextInt(1, 100_001);

        int brewerId = generator.nextInt(1, 100_001);

        int filterId = generator.nextInt(1, 100_001);

        int vesselId = generator.nextInt(1, 100_001);

        BigDecimal coffeeMass = new BigDecimal("30.0000");

        BigDecimal waterMass = new BigDecimal("500.0000");

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            context.insertInto(Brew.BREW)
                   .columns(Brew.BREW.USER_ID, Brew.BREW.TIMESTAMP, Brew.BREW.COFFEE_ID, Brew.BREW.WATER_ID,
                            Brew.BREW.BREWER_ID, Brew.BREW.FILTER_ID, Brew.BREW.VESSEL_ID, Brew.BREW.COFFEE_MASS,
                            Brew.BREW.WATER_MASS)
                   .values(userId, timestamp, coffeeId, waterId, brewerId, filterId, vesselId, coffeeMass, waterMass)
                   .execute();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        } //end try catch
    } //createRandomBrew

    private static void createRandomCoffee() {
        RandomGenerator generator = RandomGenerator.getDefault();

        int userId = generator.nextInt(1, 4);

        String coffee = UUID.randomUUID()
                            .toString();

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            context.insertInto(Coffee.COFFEE)
                   .columns(Coffee.COFFEE.USER_ID, Coffee.COFFEE.NAME)
                   .values(userId, coffee)
                   .execute();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        } //end try catch
    } //createRandomCoffee

    private static void createRandomWater() {
        RandomGenerator generator = RandomGenerator.getDefault();

        int userId = generator.nextInt(1, 4);

        String water = UUID.randomUUID()
                            .toString();

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            context.insertInto(Water.WATER)
                   .columns(Water.WATER.USER_ID, Water.WATER.NAME)
                   .values(userId, water)
                   .execute();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        } //end try catch
    } //createRandomWater

    private static void createRandomBrewer() {
        RandomGenerator generator = RandomGenerator.getDefault();

        int userId = generator.nextInt(1, 4);

        String brewer = UUID.randomUUID()
                            .toString();

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            context.insertInto(Brewer.BREWER)
                   .columns(Brewer.BREWER.USER_ID, Brewer.BREWER.NAME)
                   .values(userId, brewer)
                   .execute();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        } //end try catch
    } //createRandomBrewer

    private static void createRandomFilter() {
        RandomGenerator generator = RandomGenerator.getDefault();

        int userId = generator.nextInt(1, 4);

        String filter = UUID.randomUUID()
                            .toString();

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            context.insertInto(Filter.FILTER)
                   .columns(Filter.FILTER.USER_ID, Filter.FILTER.NAME)
                   .values(userId, filter)
                   .execute();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        } //end try catch
    } //createRandomFilter

    private static void createRandomVessel() {
        RandomGenerator generator = RandomGenerator.getDefault();

        int userId = generator.nextInt(1, 4);

        String vessel = UUID.randomUUID()
                            .toString();

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            context.insertInto(Vessel.VESSEL)
                   .columns(Vessel.VESSEL.USER_ID, Vessel.VESSEL.NAME)
                   .values(userId, vessel)
                   .execute();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        } //end try catch
    } //createRandomVessel

    public static void main(String[] args) {
        for (int i = 0; i < 100_000; i++) {
            DataFiller.createRandomCoffee();

            DataFiller.createRandomWater();

            DataFiller.createRandomBrewer();

            DataFiller.createRandomFilter();

            DataFiller.createRandomVessel();
        } //end for
    } //main
}