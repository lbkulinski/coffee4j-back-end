package com.coffee4j;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;

/**
 * The REST controller used to interact with the {@code users} MongoDB collection.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 16, 2022
 */
@RestController
@RequestMapping("api/schemas")
public final class SchemaController {
    /**
     * The name of the {@code schemas} MongoDB collection.
     */
    private static final String COLLECTION_NAME;

    /**
     * The valid field types of the {@code schemas} MongoDB collection.
     */
    private static final Set<String> VALID_FIELD_TYPES;

    static {
        COLLECTION_NAME = "schemas";

        VALID_FIELD_TYPES = Set.of(
            "double",
            "string",
            "bool",
            "date",
            "int"
        );
    } //static

    private Map<String, Map<String, String>> getFields(Map<String, Object> parameters) {
        String fieldsKey = "fields";

        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> fields = Utilities.getParameter(parameters, fieldsKey, Map.class);

        if (fields == null) {
            return null;
        } //end if

        List<Map.Entry<?, ?>> entries = new ArrayList<>();

        for (Map.Entry<?, ?> entry : fields.entrySet()) {
            Object key = entry.getKey();

            if (!(key instanceof String)) {
                return null;
            } //end if

            Object value = entry.getValue();

            if (!(value instanceof Map<?, ?> valueMap)) {
                return null;
            } //end if

            Set<? extends Map.Entry<?, ?>> valueEntries = valueMap.entrySet();

            entries.addAll(valueEntries);
        } //end for

        for (Map.Entry<?, ?> entry : entries) {
            Object key = entry.getKey();

            Object value = entry.getValue();

            if (!(key instanceof String) || !(value instanceof String)) {
                return null;
            } //end if

            String displayNameKey = "displayName";

            String typeKey = "type";

            if (!Objects.equals(key, displayNameKey) && !Objects.equals(key, typeKey)) {
                return null;
            } else if (Objects.equals(key, typeKey) && !SchemaController.VALID_FIELD_TYPES.contains(value)) {
                return null;
            } //end if
        } //end for

        return fields;
    } //getFields

    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> parameters) {
        Map<String, Map<String, String>> fields = this.getFields(parameters);

        if (fields == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A set of fields are missing or the specified set is malformed"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String defaultKey = "default";

        Boolean defaultFlag = Utilities.getParameter(parameters, defaultKey, Boolean.class);

        if (defaultFlag == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A default flag is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String sharedKey = "shared";

        Boolean sharedFlag = Utilities.getParameter(parameters, sharedKey, Boolean.class);

        if (sharedFlag == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A shared flag is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        String creatorIdKey = "creatorId";

        String creatorId = Utilities.getParameter(parameters, creatorIdKey, String.class);

        if (creatorId == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A creator ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        creatorId = creatorId.strip();

        ObjectId creatorObjectId;

        try {
            creatorObjectId = new ObjectId(creatorId);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The given creator ID has an invalid hexadecimal representation"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end try catch

        Map<String, Object> schemaMap = Map.of(
            "fields", fields,
            "default", defaultFlag,
            "shared", sharedFlag,
            "creatorId", creatorObjectId
        );

        Document schemaDocument = new Document(schemaMap);

        MongoCollection<Document> collection = Utilities.getCollection(SchemaController.COLLECTION_NAME);

        if (defaultFlag) {
            String creatorIdFieldName = "creatorId";

            Bson filter = Filters.eq(creatorIdFieldName, creatorObjectId);

            String defaultFieldName = "default";

            Bson update = Updates.set(defaultFieldName, false);

            collection.updateMany(filter, update);
        } //end if

        collection.insertOne(schemaDocument);

        Map<String, Object> successMap = Map.of(
            "success", true
        );

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //create

    @GetMapping("read")
    public ResponseEntity<?> read(@RequestParam Map<String, Object> parameters) {
        String defaultKey = "default";

        Boolean defaultFlag = Utilities.getParameter(parameters, defaultKey, Boolean.class);

        Set<Bson> filters = new HashSet<>();

        if (defaultFlag != null) {
            Bson filter = Filters.eq(defaultKey, defaultFlag);

            filters.add(filter);
        } //end if

        String sharedKey = "shared";

        Boolean sharedFlag = Utilities.getParameter(parameters, sharedKey, Boolean.class);

        if (sharedFlag != null) {
            Bson filter = Filters.eq(sharedKey, sharedFlag);

            filters.add(filter);
        } //end if

        String creatorIdKey = "creatorId";

        String creatorId = Utilities.getParameter(parameters, creatorIdKey, String.class);

        if (creatorId != null) {
            ObjectId creatorObjectId;

            try {
                creatorObjectId = new ObjectId(creatorId);
            } catch (IllegalArgumentException e) {
                creatorObjectId = null;
            } //end try catch

            if (creatorObjectId != null) {
                Bson filter = Filters.eq(creatorIdKey, creatorObjectId);

                filters.add(filter);
            } //end if
        } //end if

        return new ResponseEntity<>(HttpStatus.OK);
    } //read

    /*
{
  "_id": {
    "$oid": "621a71c889b7a08edddc9865"
  },
  "default": false,
  "shared": true,
  "creatorId": {
    "$oid": "622bd0ecd606500686f74ce7"
  }
}
     */
}