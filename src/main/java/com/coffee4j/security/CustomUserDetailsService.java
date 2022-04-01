package com.coffee4j.security;

import com.coffee4j.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        Connection connection = Utilities.getConnection();

        if (connection == null) {
            throw new IllegalStateException();
        } //end if

        String userQuery = """
            SELECT
                `id`,
                `username`,
                `password_hash`
            FROM
                `users`
            WHERE
                `username` = ?""";

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        int rowId;

        String rowUsername;

        String rowPasswordHash;

        try {
            preparedStatement = connection.prepareStatement(userQuery);

            preparedStatement.setString(1, username);

            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                String message = "A user with the username \"%s\" could not be found".formatted(username);

                throw new UsernameNotFoundException(message);
            } //end if

            rowId = resultSet.getInt("id");

            rowUsername = resultSet.getString("username");

            rowPasswordHash = resultSet.getString("password_hash");
        } catch (SQLException e) {
            CustomUserDetailsService.LOGGER.atError()
                                           .withThrowable(e)
                                           .log();

            throw new IllegalStateException();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                CustomUserDetailsService.LOGGER.atError()
                                               .withThrowable(e)
                                               .log();
            } //end try catch

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    CustomUserDetailsService.LOGGER.atError()
                                                   .withThrowable(e)
                                                   .log();
                } //end try catch
            } //end if

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    CustomUserDetailsService.LOGGER.atError()
                                                   .withThrowable(e)
                                                   .log();
                } //end try catch
            } //end if
        } //end try catch finally

        return new User(rowId, rowUsername, rowPasswordHash);
    } //loadUserByUsername
}