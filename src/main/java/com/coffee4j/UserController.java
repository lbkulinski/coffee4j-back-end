package com.coffee4j;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user")
public final class UserController {
    private static final String COLLECTION_NAME = "users";

    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> parameters) {
        String firstNameKey = "firstName";

        String firstName = Utilities.getParameter(parameters, firstNameKey, String.class);

        if (firstName == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A first name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        firstName = firstName.strip();

        Document userDocument = new Document();

        userDocument.put(firstNameKey, firstName);

        String lastNameKey = "lastName";

        String lastName = Utilities.getParameter(parameters, lastNameKey, String.class);

        if (lastName == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A last name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        lastName = lastName.strip();

        userDocument.put(lastNameKey, lastName);

        String emailKey = "email";

        String email = Utilities.getParameter(parameters, emailKey, String.class);

        if (email == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "An email is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        email = email.strip();

        userDocument.put(emailKey, email);

        MongoCollection<Document> collection = Utilities.getCollection(UserController.COLLECTION_NAME);

        collection.insertOne(userDocument);

        Map<String, Object> successMap = Map.of(
            "success", true
        );

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //create

    @GetMapping("read")
    public ResponseEntity<?> read(@RequestParam Map<String, Object> parameters) {
        String idKey = "id";

        String id = Utilities.getParameter(parameters, idKey, String.class);

        if (id == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "An ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        id = id.strip();

        ObjectId objectId;

        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "The given ID has an invalid hexadecimal representation"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end try catch

        String fieldName = "_id";

        Bson filter = Filters.eq(fieldName, objectId);

        MongoCollection<Document> collection = Utilities.getCollection(UserController.COLLECTION_NAME);

        FindIterable<Document> iterable = collection.find(filter);

        Document userDocument = iterable.first();

        if (userDocument == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A user with the given ID could not be found"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.OK);
        } //end if

        Map<String, Object> successMap = Map.of(
            "success", true,
            "user", userDocument
        );

        return new ResponseEntity<>(successMap, HttpStatus.OK);
    } //read
}