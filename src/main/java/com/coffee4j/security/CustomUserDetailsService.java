package com.coffee4j.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Objects;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.Table;
import org.jooq.Record;
import java.sql.Connection;
import java.sql.DriverManager;
import com.coffee4j.Utilities;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import java.sql.SQLException;

/**
 * A user details service of the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 23, 2022
 */
public final class CustomUserDetailsService implements UserDetailsService {
    /**
     * The {@link Logger} of the {@link CustomUserDetailsService} class.
     */
    private static final Logger LOGGER;

    static {
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

        Field<Integer> idField = DSL.field("id", Integer.class);

        Field<String> usernameField = DSL.field("username", String.class);

        Field<String> passwordHashField = DSL.field("password_hash", String.class);

        Table<Record> usersTable = DSL.table("users");

        Record record;

        try (Connection connection = DriverManager.getConnection(Utilities.DATABASE_URL)) {
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.select(idField, usernameField, passwordHashField)
                            .from(usersTable)
                            .where(usernameField.eq(username))
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

        int recordId = record.getValue(idField);

        String recordUsername = record.getValue(usernameField);

        String recordPasswordHash = record.getValue(passwordHashField);

        return new User(recordId, recordUsername, recordPasswordHash);
    } //loadUserByUsername
}