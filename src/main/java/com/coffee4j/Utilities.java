package com.coffee4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * A set of utilities used by the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@icloud.com
 * @version April 22, 2022
 */
public final class Utilities {
    /**
     * The {@link Logger} of the {@link Utilities} class.
     */
    private static final Logger LOGGER;

    /**
     * The database URL of the {@link Utilities} class.
     */
    public static final String DATABASE_URL;

    static {
        LOGGER = LogManager.getLogger();

        String uri = null;

        try {
            String pathString = "src/main/resources/database.properties";

            Path path = Path.of(pathString);

            BufferedReader reader = Files.newBufferedReader(path);

            Properties properties = new Properties();

            properties.load(reader);

            String uriKey = "database_url";

            uri = properties.getProperty(uriKey);
        } catch (IOException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();
        } //end try catch

        DATABASE_URL = uri;
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
}