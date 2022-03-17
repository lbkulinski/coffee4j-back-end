package com.coffee4j;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * The REST controller used to interact with the {@code schemas} MongoDB collection.
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
    private static final String COLLECTION_NAME = "schemas";

    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> parameters) {
        String fieldsKey = "fields";

        @SuppressWarnings("unchecked")
        Map<String, String> fields = (Map<String, String>) Utilities.getParameter(parameters, fieldsKey, Map.class);

        if (fields == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A set of fields is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        for (Map.Entry<?, ?> field : fields.entrySet()) {
            Object fieldKey = field.getKey();

            Object fieldValue = field.getValue();

            if (!(fieldKey instanceof String)) {
                Map<String, Object> errorMap = Map.of(
                    "success", false,
                    "message", "A field's name must be a string"
                );

                return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
            } else if (!(fieldValue instanceof Map)) {
                Map<String, Object> errorMap = Map.of(
                    "success", false,
                    "message", "A field's attributes must be an object whose keys and values are strings"
                );

                return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
            } //end if

            Map<?, ?> attributes = (Map<?, ?>) fieldValue;

            for (Map.Entry<?, ?> attribute : attributes.entrySet()) {
                Object attributeKey = attribute.getKey();

                Object attributeValue = attribute.getValue();

                if (!(attributeKey instanceof String) || !(attributeValue instanceof String)) {
                    Map<String, Object> errorMap = Map.of(
                        "success", false,
                        "message", "A field attribute's key and value must be a string"
                    );

                    return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
                } //end if
            } //end for
        } //end for

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

        Boolean shared = Utilities.getParameter(parameters, sharedKey, Boolean.class);

        if (shared == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A default flag is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        return new ResponseEntity<>(HttpStatus.OK);
    } //create

/*
{
  "_id": {
    "$oid": "621a71c889b7a08edddc9865"
  },
  "fields": {
    "date": {
      "displayName": "Date",
      "type": "date"
    },
    "coffee": {
      "displayName": "Coffee",
      "type": "string"
    },
    "water": {
      "displayName": "Water",
      "type": "string"
    },
    "brewer": {
      "displayName": "Brewer",
      "type": "string"
    },
    "filter": {
      "displayName": "Filter",
      "type": "string"
    },
    "vessel": {
      "displayName": "Vessel",
      "type": "string"
    },
    "coffeeMass": {
      "displayName": "Coffee Mass (g)",
      "type": "double"
    },
    "waterMass": {
      "displayName": "Water Mass (g)",
      "type": "double"
    }
  },
  "default": true,
  "shared": true,
  "userId": {
    "$oid": "622bd0ecd606500686f74ce7"
  }
}
*/
}