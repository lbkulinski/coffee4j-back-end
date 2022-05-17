package com.coffee4j.model.json;

import com.coffee4j.model.Field;
import com.coffee4j.model.Schema;
import com.coffee4j.model.Type;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class SchemaDeserializer extends JsonDeserializer<Schema> {
    private static final Logger LOGGER;

    private static final String ERROR;

    static {
        LOGGER = LogManager.getLogger();

        ERROR = "the specified JSON node could not be mapped to a Schema object";
    } //static

    private Field deserializeField(JsonParser parser, JsonNode jsonNode) throws JsonMappingException {
        JsonNode nameNode = jsonNode.get("name");

        if ((nameNode == null) || !nameNode.isTextual()) {
            throw JsonMappingException.from(parser, ERROR);
        } //end if

        String name = nameNode.asText();

        JsonNode displayNameNode = jsonNode.get("display_name");

        if ((displayNameNode == null) || !displayNameNode.isTextual()) {
            throw JsonMappingException.from(parser, ERROR);
        } //end if

        String displayName = displayNameNode.asText();

        JsonNode typeNode = jsonNode.get("type");

        if ((typeNode == null) || !typeNode.isTextual()) {
            throw JsonMappingException.from(parser, ERROR);
        } //end if

        String typeString = typeNode.asText();

        typeString = typeString.toUpperCase();

        Type type;

        try {
            type = Type.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            throw JsonMappingException.from(parser, ERROR);
        } //end try catch

        return new Field(name, displayName, type);
    } //deserializeField

    @Override
    public Schema deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectCodec objectCodec = parser.getCodec();

        JsonNode jsonNode = objectCodec.readTree(parser);

        JsonNode idNode = jsonNode.get("id");

        Integer id = null;

        if ((idNode != null) && idNode.isInt()) {
            id = idNode.asInt();
        } //end if

        JsonNode ownerIdNode = jsonNode.get("owner_id");

        Integer ownerId = null;

        if ((ownerIdNode != null) && ownerIdNode.isInt()) {
            ownerId = ownerIdNode.asInt();
        } //end if

        JsonNode nameNode = jsonNode.get("name");

        String name = null;

        if ((nameNode != null) && nameNode.isTextual()) {
            name = nameNode.asText();
        } //end if

        JsonNode defaultFlagNode = jsonNode.get("default");

        Boolean defaultFlag = null;

        if ((defaultFlagNode != null) && defaultFlagNode.isBoolean()) {
            defaultFlag = defaultFlagNode.asBoolean();
        } //end if

        JsonNode sharedFlagNode = jsonNode.get("shared");

        Boolean sharedFlag = null;

        if ((sharedFlagNode != null) && sharedFlagNode.isBoolean()) {
            sharedFlag = sharedFlagNode.asBoolean();
        } //end if

        JsonNode fieldsNode = jsonNode.get("fields");

        List<Field> fields = null;

        if ((fieldsNode != null) && fieldsNode.isArray()) {
            fields = new ArrayList<>();

            for (JsonNode element : fieldsNode) {
                Field field = this.deserializeField(parser, element);

                fields.add(field);
            } //end while
        } //end if

        return new Schema(id, ownerId, name, defaultFlag, sharedFlag, fields);
    } //deserialize
}