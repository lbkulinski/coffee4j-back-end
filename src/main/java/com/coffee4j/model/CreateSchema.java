package com.coffee4j.model;

import com.coffee4j.model.json.CreateSchemaDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;
import java.util.Set;

@JsonDeserialize(using = CreateSchemaDeserializer.class)
public record CreateSchema(String name, boolean defaultFlag, boolean sharedFlag, Set<Field> fields) {
    public record Field(String name, String displayName, Type type) {
        public Field {
            Objects.requireNonNull(name, "the specified name is null");

            Objects.requireNonNull(displayName, "the specified display name is null");

            Objects.requireNonNull(type, "the specified type is null");
        } //Field
    } //Field

    public CreateSchema {
        Objects.requireNonNull(name, "the specified name is null");

        Objects.requireNonNull(fields, "the specified Set of fields is null");

        for (Field field : fields) {
            Objects.requireNonNull(field, "a field in the specified Set of fields is null");
        } //end for
    } //CreateSchema
}