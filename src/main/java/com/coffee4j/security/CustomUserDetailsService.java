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
import schema.generated.tables.Users;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A user details service of the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 26, 2022
 */
public final class CustomUserDetailsService implements UserDetailsService {
    /**
     * The {@code users} table of the {@link CustomUserDetailsService} class.
     */
    private static final Users USERS;

    /**
     * The {@link Logger} of the {@link CustomUserDetailsService} class.
     */
    private static final Logger LOGGER;

    static {
        USERS = Users.USERS;

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
            DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

            record = context.select()
                            .from(USERS)
                            .where(USERS.USERNAME.eq(username))
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

        int recordId = record.get(USERS.ID);

        String recordUsername = record.get(USERS.USERNAME);

        String recordPasswordHash = record.get(USERS.PASSWORD_HASH);

        return new User(recordId, recordUsername, recordPasswordHash);
    } //loadUserByUsername
}