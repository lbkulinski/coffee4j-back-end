package com.coffee4j;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public final class Utilities {
    private static final Logger LOGGER;

    private static final String URI;

    public static final String DATABASE_NAME;

    static {
        LOGGER = LogManager.getLogger();

        String uri = null;

        String databaseName = null;

        try {
            String pathString = "src/main/resources/database.properties";

            Path path = Path.of(pathString);

            BufferedReader reader = Files.newBufferedReader(path);

            Properties properties = new Properties();

            properties.load(reader);

            String uriKey = "uri";

            uri = properties.getProperty(uriKey);

            String databaseNameKey = "databaseName";

            databaseName = properties.getProperty(databaseNameKey);
        } catch (IOException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();
        } //end try catch

        URI = uri;

        DATABASE_NAME = databaseName;
    } //static

    private Utilities() throws InstantiationException {
        throw new InstantiationException("an instance of the Utilities class cannot be created");
    } //Utilities

    public static <T> T getParameter(Map<String, Object> parameters, String key, Class<T> clazz) {
        Objects.requireNonNull(parameters, "the specified Map of parameters is null");

        Objects.requireNonNull(key, "the specified key is null");

        Objects.requireNonNull(clazz, "the specified Class is null");

        if (!parameters.containsKey(key)) {
            return null;
        } //end if

        Object object = parameters.get(key);

        Class<?> objectClass = object.getClass();

        if (objectClass != clazz) {
            return null;
        } //end if

        return clazz.cast(object);
    } //getValue

    public static MongoCollection<Document> getCollection(String collectionName) {
        Objects.requireNonNull(collectionName, "the specified collection name is null");

        MongoClient client = MongoClients.create(Utilities.URI);

        MongoDatabase database = client.getDatabase(Utilities.DATABASE_NAME);

        return database.getCollection(collectionName);
    } //getCollection
}