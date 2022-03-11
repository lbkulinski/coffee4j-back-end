package com.coffee4j;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The REST controller used to interact with the {@code users} MongoDB collection.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 11, 2022
 */
@RestController
@RequestMapping("api/user")
public final class UserController {
    /**
     * The name of the {@code users} MongoDB collection.
     */
    private static final String COLLECTION_NAME = "users";

    /**
     * Attempts to create a user with the specified parameters. A first name, last name, and email is required to
     * create a user.
     *
     * @param parameters the parameters to be used in the operation
     * @return the response to attempting to create a user with the specified parameters
     */
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

    /**
     * Attempts to read a user's data with the specified parameters. An object ID is required to read a user's data.
     *
     * @param parameters the parameters to be used in the operation
     * @return the response to attempting to read a user's data with the specified parameters
     */
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

    /**
     * Attempts to update a user's data with the specified parameters. An object ID is required to update a user's
     * data. Updates to a user's first name, last name, and email can be made.
     *
     * @param parameters the parameters to be used in the operation
     * @return the response to attempting to update a user's data with the specified parameters
     */
    @PostMapping("update")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> parameters) {
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

        String idFieldName = "_id";

        Bson filter = Filters.eq(idFieldName, objectId);

        String firstNameKey = "firstName";

        String firstName = Utilities.getParameter(parameters, firstNameKey, String.class);

        List<Bson> updates = new ArrayList<>();

        if (firstName != null) {
            String fieldName = "firstName";

            Bson update = Updates.set(fieldName, firstName);

            updates.add(update);
        } //end if

        String lastNameKey = "lastName";

        String lastName = Utilities.getParameter(parameters, lastNameKey, String.class);

        if (lastName != null) {
            String fieldName = "lastName";

            Bson update = Updates.set(fieldName, lastName);

            updates.add(update);
        } //end if

        String emailKey = "email";

        String email = Utilities.getParameter(parameters, emailKey, String.class);

        if (email != null) {
            String fieldName = "email";

            Bson update = Updates.set(fieldName, email);

            updates.add(update);
        } //end if

        Bson[] updateArray = updates.toArray(Bson[]::new);

        Bson combinedUpdates = Updates.combine(updateArray);

        MongoCollection<Document> collection = Utilities.getCollection(UserController.COLLECTION_NAME);

        UpdateResult updateResult = collection.updateOne(filter, combinedUpdates);

        long modifiedCount = updateResult.getModifiedCount();

        Map<String, Object> responseMap;

        if (modifiedCount == 0) {
            responseMap = Map.of(
                "success", false,
                "message", "The update could not be performed"
            );
        } else {
            responseMap = Map.of(
                "success", true
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //update

    /**
     * Attempts to delete a user's data with the specified parameters. An object ID is required to delete a user's
     * data.
     *
     * @param parameters the parameters to be used in the operation
     * @return the response to attempting to delete a user's data with the specified parameters
     */
    @PostMapping("delete")
    public ResponseEntity<?> delete(@RequestBody Map<String, Object> parameters) {
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

        DeleteResult deleteResult = collection.deleteOne(filter);

        long deletedCount = deleteResult.getDeletedCount();

        Map<String, Object> responseMap;

        if (deletedCount == 0) {
            responseMap = Map.of(
                    "success", false,
                    "message", "The deletion could not be performed"
            );
        } else {
            responseMap = Map.of(
                    "success", true
            );
        } //end if

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    } //delete
}