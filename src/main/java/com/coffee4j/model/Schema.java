package com.coffee4j.model;

import com.coffee4j.model.json.SchemaDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(using = SchemaDeserializer.class)
public record Schema(Integer id, Integer ownerId, String name, Boolean defaultFlag, Boolean sharedFlag,
                     List<Field> fields) {
}