package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * A set of utility methods used by the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version March 21, 2022
 */
public final class Utilities {
    /**
     * The {@link Logger} of the {@link Utilities} class.
     */
    private static final Logger LOGGER;

    /**
     * The database URI of the {@link Utilities} class.
     */
    private static final String URI;

    static {
        LOGGER = LogManager.getLogger();

        String uri = null;

        try {
            String pathString = "src/main/resources/database.properties";

            Path path = Path.of(pathString);

            BufferedReader reader = Files.newBufferedReader(path);

            Properties properties = new Properties();

            properties.load(reader);

            String uriKey = "uri";

            uri = properties.getProperty(uriKey);
        } catch (IOException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();
        } //end try catch

        URI = uri;
    } //static

    /**
     * Throws an {@link InstantiationException}, as an instance of the {@link Utilities} class cannot be created.
     *
     * @throws InstantiationException if this constructor is invoked, as an instance of the {@link Utilities} class
     * cannot be created
     */
    private Utilities() throws InstantiationException {
        throw new InstantiationException("an instance of the Utilities class cannot be created");
    } //Utilities

    /**
     * Returns the parameter from the specified parameters that is associated with the specified key and is of the
     * specified {@link Class} or {@code null} if one does not exist.
     *
     * @param parameters the parameters to be used in the operation
     * @param key the key to be used in the operation
     * @param clazz the {@link Class} to be used in the operation
     * @param <T> the type to be used in the operation
     * @return the parameter from the specified parameters that is associated with the specified key and is of the
     * specified {@link Class} or {@code null} if one does not exist
     * @throws NullPointerException if the specified {@link Map} of parameters, key, or {@link Class} is {@code null}
     */
    public static <T> T getParameter(Map<String, Object> parameters, String key, Class<T> clazz) {
        Objects.requireNonNull(parameters, "the specified Map of parameters is null");

        Objects.requireNonNull(key, "the specified key is null");

        Objects.requireNonNull(clazz, "the specified Class is null");

        if (!parameters.containsKey(key)) {
            return null;
        } //end if

        Object object = parameters.get(key);

        Class<?> objectClass = object.getClass();

        if (!clazz.isAssignableFrom(objectClass)) {
            return null;
        } //end if

        return clazz.cast(object);
    } //getValue

    /**
     * Returns a {@link Connection} to the Coffee4j database or {@code null} if one could not be made
     *
     * @return a {@link Connection} to the Coffee4j database or {@code null} if one could not be made
     */
    public static Connection getConnection() {
        Connection connection;

        try {
            connection = DriverManager.getConnection(Utilities.URI);
        } catch (SQLException e) {
            Utilities.LOGGER.atError()
                            .withThrowable(e)
                            .log();

            return null;
        } //end try catch

        return connection;
    } //getConnection
}