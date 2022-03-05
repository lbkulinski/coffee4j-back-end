package com.coffee4j;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        firstName = firstName.strip();

        Document userDocument = new Document();

        userDocument.put(firstNameKey, firstName);

        String lastNameKey = "lastName";

        String lastName = Utilities.getParameter(parameters, lastNameKey, String.class);

        if (lastName == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } //end if

        lastName = lastName.strip();

        userDocument.put(lastNameKey, lastName);

        String emailKey = "email";

        String email = Utilities.getParameter(parameters, emailKey, String.class);

        if (email == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
}