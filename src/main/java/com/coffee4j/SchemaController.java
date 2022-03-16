package com.coffee4j;

import com.coffee4j.model.Schema;
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
    public ResponseEntity<?> create(@RequestBody Schema schema) {
        System.out.println(schema);

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