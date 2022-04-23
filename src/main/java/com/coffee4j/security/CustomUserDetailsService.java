package com.coffee4j.security;

import com.coffee4j.Utilities;
import static com.coffee4j.generated.tables.Users.USERS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A user details service of the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 1, 2022
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

            throw new IllegalStateException();
        } //end try catch

        if (record == null) {
            throw new IllegalStateException();
        } //end if

        int rowId = record.get(USERS.ID);

        String rowUsername = record.get(USERS.USERNAME);

        byte[] rowPasswordHashBytes = record.get(USERS.PASSWORD_HASH);

        String rowPasswordHash = new String(rowPasswordHashBytes);

        return new User(rowId, rowUsername, rowPasswordHash);
    } //loadUserByUsername
}