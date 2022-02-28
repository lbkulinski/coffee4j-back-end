package com.coffee4j;

import com.mongodb.DBRef;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
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

    private final String collectionName;

    public SchemaController() {
        this.collectionName = "schemas";
    } //SchemaController

    @PostMapping("create")
    public ResponseEntity<Document> create(@RequestBody Map<String, Object> parameters) {
        System.out.println(parameters);

        String userIdKey = "userId";

        if (!parameters.containsKey(userIdKey)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        Object userIdObject = parameters.get(userIdKey);

        if (!(userIdObject instanceof String userId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

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

        MongoCollection<Document> collection = database.getCollection(this.collectionName);

        Document schemaDocument = new Document();

        String userKey = "user";

        String usersCollectionName = "users";

        ObjectId userObjectId = new ObjectId(userId);

        DBRef userRef = new DBRef(this.databaseName, usersCollectionName, userObjectId);

        schemaDocument.put(userKey, userRef);

        schemaDocument.put(defaultKey, defaultFlag);

        schemaDocument.put(fieldsKey, fields);

        collection.insertOne(schemaDocument);

        String successKey = "success";

        boolean successValue = true;

        Document responseDocument = new Document(successKey, successValue);

        return new ResponseEntity<>(responseDocument, HttpStatus.OK);
    } //create

    @GetMapping("read")
    public ResponseEntity<List<Document>> read(@RequestParam String userId) {
        if ((this.uri == null) || (this.databaseName == null)) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } //end if

        MongoClient client = MongoClients.create(this.uri);

        MongoDatabase database = client.getDatabase(this.databaseName);

        MongoCollection<Document> collection = database.getCollection(this.collectionName);

        String fieldName = "user.$id";

        ObjectId userObjectId = new ObjectId(userId);

        Bson filter = Filters.eq(fieldName, userObjectId);

        FindIterable<Document> iterable = collection.find(filter);

        List<Document> documents = new ArrayList<>();

        for (Document document : iterable) {
            documents.add(document);
        } //end for

        return new ResponseEntity<>(documents, HttpStatus.OK);
    } //read
}