package com.coffee4j.controller;

import com.coffee4j.Body;
import com.coffee4j.Utilities;
import com.coffee4j.model.Field;
import com.coffee4j.model.Schema;
import com.coffee4j.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schema.generated.tables.Fields;
import schema.generated.tables.Schemas;
import schema.generated.tables.records.FieldsRecord;
import schema.generated.tables.records.SchemasRecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/schemas")
public final class SchemaController {
    private static final Logger LOGGER;

    private static final Schemas SCHEMAS;

    private static final Fields FIELDS;

    static {
        LOGGER = LogManager.getLogger();

        SCHEMAS = Schemas.SCHEMAS;

        FIELDS = Fields.FIELDS;
    } //static

    private SchemasRecord createSchema(Configuration configuration, int ownerId, Schema schema) {
        Objects.requireNonNull(configuration, "the specified configuration is null");

        Objects.requireNonNull(schema, "the specified schema is null");

        String name = schema.name();

        Boolean defaultFlag = schema.defaultFlag();

        Boolean sharedFlag = schema.sharedFlag();

        return DSL.using(configuration)
                  .insertInto(SCHEMAS)
                  .columns(SCHEMAS.OWNER_ID, SCHEMAS.NAME, SCHEMAS.DEFAULT, SCHEMAS.SHARED)
                  .values(ownerId, name, defaultFlag, sharedFlag)
                  .returning()
                  .fetchOne();
    } //createSchema

    private void createField(Configuration configuration, int schemaId, Field field) {
        Objects.requireNonNull(configuration, "the specified configuration is null");

        Objects.requireNonNull(field, "the specified field is null");

        String name = field.name();

        String displayName = field.displayName();

        int typeId = field.type()
                          .getId();

        DSL.using(configuration)
           .insertInto(FIELDS)
           .columns(FIELDS.SCHEMA_ID, FIELDS.NAME, FIELDS.DISPLAY_NAME, FIELDS.TYPE_ID)
           .values(schemaId, name, displayName, typeId)
           .execute();
    } //createField

    @PostMapping
    public ResponseEntity<Body<?>> create(@RequestBody Schema schema) {
        User user = Utilities.getLoggedInUser();

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } //end if

        int ownerId = user.id();

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            context.transaction(configuration -> {
                SchemasRecord schemasRecord = this.createSchema(configuration, ownerId, schema);

                int schemaId = schemasRecord.getId();

                List<Field> fields = schema.fields();

                for (Field field : fields) {
                    this.createField(configuration, schemaId, field);
                } //end for
            });
        } catch (SQLException | DataAccessException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            String content = "A schema with the specified parameters could not be created";

            Body<?> body = Body.error(content);

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } //end try catch

        return new ResponseEntity<>(HttpStatus.OK);
    } //create
}