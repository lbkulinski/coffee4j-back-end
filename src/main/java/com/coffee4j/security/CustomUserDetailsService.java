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

package com.coffee4j.security;

import com.coffee4j.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A user details service of the Coffee4j application.
 *
 * @author Logan Kulinski, rashes_lineage02@icloud.com
 * @version July 11, 2022
 */
public final class CustomUserDetailsService implements UserDetailsService {
    /**
     * The {@code user} table of the {@link CustomUserDetailsService} class.
     */
    private static final schema.generated.tables.User USER;

    /**
     * The {@link Logger} of the {@link CustomUserDetailsService} class.
     */
    private static final Logger LOGGER;

    static {
        USER = schema.generated.tables.User.USER;

        LOGGER = LogManager.getLogger();
    } //static

    /**
     * Returns the {@link UserDetails} of the user with the specified username.
     *
     * @param username the username to be used in the operation
     * @return the {@link UserDetails} of the user with the specified username
     * @throws UsernameNotFoundException if a user with the specified username could not be found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Objects.requireNonNull(username, "the specified username is null");

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);

            record = context.select()
                            .from(USER)
                            .where(USER.USERNAME.eq(username))
                            .fetchOne();
        } catch (SQLException e) {
            LOGGER.atError()
                  .withThrowable(e)
                  .log();

            throw new IllegalStateException(e);
        } //end try catch

        if (record == null) {
            String message = "A user with the username \"%s\" could not be found".formatted(username);

            throw new UsernameNotFoundException(message);
        } //end if

        int recordId = record.get(USER.ID);

        String recordUsername = record.get(USER.USERNAME);

        String recordPasswordHash = record.get(USER.PASSWORD_HASH);

        return new User(recordId, recordUsername, recordPasswordHash);
    } //loadUserByUsername
}