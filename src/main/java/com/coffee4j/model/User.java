package com.coffee4j.model;

import org.bson.types.ObjectId;

public record User(ObjectId id, String firstName, String lastName, String email) {
}