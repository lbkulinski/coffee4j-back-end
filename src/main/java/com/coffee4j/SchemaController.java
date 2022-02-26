package com.coffee4j;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/schema")
public final class SchemaController {
    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

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