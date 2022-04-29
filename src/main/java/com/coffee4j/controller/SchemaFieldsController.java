package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.Utilities;
import com.coffee4j.security.User;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import schema.generated.tables.SchemaFields;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RestController
@RequestMapping("api/schema_fields")
public final class SchemaFieldsController {
    /**
     * The {@code schema_fields} table of the {@link SchemaFieldsController} class.
     */
    private static final SchemaFields SCHEMA_FIELDS;

    /**
     * The {@link Logger} of the {@link SchemaFieldsController} class.
     */
    private static final Logger LOGGER;

    static {
        SCHEMA_FIELDS = SchemaFields.SCHEMA_FIELDS;

        LOGGER = LogManager.getLogger();
    } //static

    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestParam("schema_id") int schemaId,
                                          @RequestParam("field_id") int fieldId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        //TODO: Make sure the owner of both is the current logged in user

        int rowsChanged;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            rowsChanged = context.insertInto(SCHEMA_FIELDS)
                                 .columns(SCHEMA_FIELDS.SCHEMA_ID, SCHEMA_FIELDS.FIELD_ID)
                                 .values(schemaId, fieldId)
                                 .execute();
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "An association with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        if (rowsChanged == 0) {
            String content = "An association with the specified parameters could not be created";

            Body<String> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } //end if

        String content = "An association with the specified parameters was successfully created";

        Body<String> body = Body.success(content);

        return new ResponseEntity<>(body, HttpStatus.OK);
    } //create

    @GetMapping
    public ResponseEntity<Body<?>> read(@RequestParam(name = "schema_id", required = false) int schemaId) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        /*
        SELECT
            `s`.`id` AS `schema_id`,
            `s`.`owner_id`,
            `s`.`name` AS `schema_name`,
            `s`.`default`,
            `s`.`shared`,
            `f`.`id` AS `field_id`,
            `f`.`name` AS `field_name`,
            `f`.`display_name` AS `field_display_name`,
            `ft`.`id` AS `field_type_id`,
            `ft`.`name` AS `field_type_name`
        FROM
            `schemas` `s`
                INNER JOIN
            `schema_fields` `sf` ON `s`.`id` = `sf`.`schema_id`
                INNER JOIN
            `fields` `f` ON `sf`.`field_id` = `f`.`id`
                INNER JOIN
            `field_types` `ft` ON `ft`.`id` = `f`.`type_id`
         */

        return new ResponseEntity<>(HttpStatus.OK);
    } //read
}