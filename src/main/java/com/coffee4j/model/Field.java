package com.coffee4j.model;

import java.util.Objects;

public record Field(String name, String displayName, Type type) {
    public Field {
        Objects.requireNonNull(name, "the specified name is null");

        Objects.requireNonNull(displayName, "the specified display name is null");

        Objects.requireNonNull(type, "the specified type is null");
    } //Field
} //Field