package com.coffee4j;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/schema")
public final class SchemaController {
    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @PostMapping("create")
    public ResponseEntity<Document> create(@RequestBody Map<String, Object> parameters) {
        System.out.println(parameters);

        String userIdKey = "userId";

        if (!parameters.containsKey(userIdKey)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        Object userIdObject = parameters.get(userIdKey);

        if (!(userIdObject instanceof Number number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        long userId = number.longValue();

        String defaultKey = "default";

        if (!parameters.containsKey(defaultKey)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        Object defaultFlagObject = parameters.get(defaultKey);

        if (!(defaultFlagObject instanceof Boolean defaultFlag)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        String fieldsKey = "fields";

        if (!parameters.containsKey(fieldsKey)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        Object fieldsObject = parameters.get(fieldsKey);

        if (!(fieldsObject instanceof Map fields)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        MongoClient client = MongoClients.create(this.uri);

        MongoDatabase database = client.getDatabase(this.databaseName);

        String collectionName = "schemas";

        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document schemaDocument = new Document();

        schemaDocument.put(userIdKey, userId);

        schemaDocument.put(defaultKey, defaultFlag);

        schemaDocument.put(fieldsKey, fields);

        collection.insertOne(schemaDocument);

        String successKey = "success";

        boolean successValue = true;

        Document responseDocument = new Document(successKey, successValue);

        return new ResponseEntity<>(responseDocument, HttpStatus.OK);
    } //create

    @GetMapping("read")
    public ResponseEntity<List<Document>> read(@RequestParam long userId) {
        if ((this.uri == null) || (this.databaseName == null)) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        MongoClient client = MongoClients.create(this.uri);

        MongoDatabase database = client.getDatabase(this.databaseName);

        String collectionName = "schemas";

        MongoCollection<Document> collection = database.getCollection(collectionName);

        String fieldName = "userId";

        Bson filter = Filters.eq(fieldName, userId);

        FindIterable<Document> iterable = collection.find(filter);

        List<Document> documents = new ArrayList<>();

        for (Document document : iterable) {
            documents.add(document);
        } //end for

        return new ResponseEntity<>(documents, HttpStatus.OK);
    } //read
}