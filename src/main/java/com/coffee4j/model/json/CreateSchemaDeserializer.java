package com.coffee4j.model.json;

import com.coffee4j.model.CreateSchema;
import com.coffee4j.model.Type;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreateSchemaDeserializer extends JsonDeserializer<CreateSchema> {
    private static final String ERROR_MESSAGE;

    static {
        ERROR_MESSAGE = "the specified JSON node could not be mapped to a CreateSchema object";
    } //static

    private Type deserializeType(JsonParser parser, JsonNode jsonNode) throws JsonMappingException {
        if (!jsonNode.has("id")) {
            throw JsonMappingException.from(parser, ERROR_MESSAGE);
        } //end if

        JsonNode idNode = jsonNode.get("id");

        if (!idNode.isInt()) {
            throw JsonMappingException.from(parser, ERROR_MESSAGE);
        } //end if

        int id = idNode.asInt();

        Type[] types = Type.values();

        for (Type type : types) {
            int typeId = type.getId();

            if (typeId == id) {
                return type;
            } //end for
        } //end for

        throw JsonMappingException.from(parser, ERROR_MESSAGE);
    } //deserializeType

    private CreateSchema.Field deserializeField(JsonParser parser, JsonNode jsonNode) throws JsonMappingException {
        if (!jsonNode.has("name")) {
            throw JsonMappingException.from(parser, ERROR_MESSAGE);
        } //end if

        JsonNode nameNode = jsonNode.get("name");

        if (!nameNode.isTextual()) {
            throw JsonMappingException.from(parser, ERROR_MESSAGE);
        } //end if

        String name = nameNode.asText();

        if (!jsonNode.has("display_name")) {
            throw JsonMappingException.from(parser, ERROR_MESSAGE);
        } //end if

        JsonNode displayNameNode = jsonNode.get("display_name");

        if (!displayNameNode.isTextual()) {
            throw JsonMappingException.from(parser, ERROR_MESSAGE);
        } //end if

        String displayName = displayNameNode.asText();

        if (!jsonNode.has("type")) {
            throw JsonMappingException.from(parser, ERROR_MESSAGE);
        } //end if

        JsonNode typeNode = jsonNode.get("type");

        Type type = this.deserializeType(parser, typeNode);

        return new CreateSchema.Field(name, displayName, type);
    } //deserializeField

    @Override
    public CreateSchema deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectCodec objectCodec = parser.getCodec();

        JsonNode jsonNode = objectCodec.readTree(parser);

        String name = jsonNode.get("name")
                              .asText();

        boolean defaultFlag = jsonNode.get("default")
                                      .asBoolean();

        boolean sharedFlag = jsonNode.get("shared")
                                     .asBoolean();

        Iterator<JsonNode> elements = jsonNode.get("fields")
                                              .elements();

        Set<CreateSchema.Field> fields = new HashSet<>();

        while (elements.hasNext()) {
            JsonNode element = elements.next();

            CreateSchema.Field field = this.deserializeField(parser, element);

            fields.add(field);
        } //end while

        return new CreateSchema(name, defaultFlag, sharedFlag, fields);
    } //deserialize
}