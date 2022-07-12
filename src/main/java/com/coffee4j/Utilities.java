/*
 * MIT License
 *
 * Copyright (c) 2022 Logan Kulinski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.coffee4j;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.Properties;
import java.io.IOException;
import com.coffee4j.security.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A set of utilities used by the Coffee4j application.
 *
 * @author Logan Kulinski, rashes_lineage02@icloud.com
 * @version July 11, 2022
 */
public final class Utilities {
    /**
     * The {@link Logger} of the {@link Utilities} class.
     */
    private static final Logger LOGGER;

    /**
     * The database URI of the {@link Utilities} class.
     */
    public static final String DATABASE_URL;

    static {
        LOGGER = LogManager.getLogger();

        String databaseUrl = null;

        try {
            String pathString = "src/main/resources/database.properties";

            Path path = Path.of(pathString);

            BufferedReader reader = Files.newBufferedReader(path);

            Properties properties = new Properties();

            properties.load(reader);

            String uriKey = "database_url";

            databaseUrl = properties.getProperty(uriKey);
        } catch (IOException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();
        } //end try catch

        DATABASE_URL = databaseUrl;
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
     * Returns the current logged-in user or {@code null} if there is none
     *
     * @return the current logged-in user or {@code null} if there is none
     */
    public static User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;
        } //end if

        return null;
    } //getLoggedInUser
}