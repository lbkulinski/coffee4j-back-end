package com.coffee4j;

import com.coffee4j.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.FindIterable;
import java.util.List;
import java.util.ArrayList;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;

/**
 * The REST controller used to interact with the {@code users} MongoDB collection.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 12, 2022
 */
@RestController
@RequestMapping("api/users")
public final class UserController {
    /**
     * The name of the {@code users} MongoDB collection.
     */
    private static final String COLLECTION_NAME = "users";

    /**
     * Attempts to create a user with the specified parameters. A first name, last name, and email is required to
     * create a user.
     *
     * @param user the parameters to be used in the operation
     * @return the response to attempting to create a user with the specified parameters
     */
    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody User user) {
        String firstName = user.firstName();

        if (firstName == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A first name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        firstName = firstName.strip();

        String lastName = user.lastName();

        if (lastName == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "A last name is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        lastName = lastName.strip();

        String email = user.email();

        if (email == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "An email is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        email = email.strip();

        Map<String, Object> userMap = Map.of(
            "firstName", firstName,
            "lastName", lastName,
            "email", email
        );

        Document userDocument = new Document(userMap);

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
     * @param id the parameters to be used in the operation
     * @return the response to attempting to read a user's data with the specified parameters
     */
    @GetMapping("read")
    public ResponseEntity<?> read(@RequestParam ObjectId id) {
        String fieldName = "_id";

        Bson filter = Filters.eq(fieldName, id);

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
     * @param user the parameters to be used in the operation
     * @return the response to attempting to update a user's data with the specified parameters
     */
    @PostMapping("update")
    public ResponseEntity<?> update(@RequestBody User user) {
        ObjectId id = user.id();

        if (id == null) {
            Map<String, Object> errorMap = Map.of(
                "success", false,
                "message", "An ID is required"
            );

            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        } //end if

        Bson filter = Filters.eq(id);

        String firstName = user.firstName();

        List<Bson> updates = new ArrayList<>();

        if (firstName != null) {
            String fieldName = "firstName";

            Bson update = Updates.set(fieldName, firstName);

            updates.add(update);
        } //end if

        String lastName = user.lastName();

        if (lastName != null) {
            String fieldName = "lastName";

            Bson update = Updates.set(fieldName, lastName);

            updates.add(update);
        } //end if

        String email = user.email();

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
     * @param id the parameters to be used in the operation
     * @return the response to attempting to delete a user's data with the specified parameters
     */
    @PostMapping("delete")
    public ResponseEntity<?> delete(@RequestBody ObjectId id) {
        Bson filter = Filters.eq(id);

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