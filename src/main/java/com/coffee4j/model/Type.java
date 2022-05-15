package com.coffee4j.model;

public enum Type {
    STRING(1),

    DATE(2),

    INT(3),

    DOUBLE(4),

    BOOLEAN(5);

    private final int id;

    Type(int id) {
        this.id = id;
    } //Type

    public int getId() {
        return this.id;
    } //getId
}