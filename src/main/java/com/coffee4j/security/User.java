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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A user of the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version May 27, 2022
 * @param id the ID of this user
 * @param username the username of this user
 * @param password the password of this user
 */
public record User(int id, String username, String password) implements UserDetails {
    /**
     * Constructs an instance of the {@link User} class.
     *
     * @param id the ID to be used in construction
     * @param username the username to be used in construction
     * @param password the password to be used in construction
     */
    public User {
        Objects.requireNonNull(username, "the specified username is null");

        Objects.requireNonNull(password, "the specified password is null");
    } //User

    /**
     * Returns the username of this user.
     *
     * @return the username of this user
     */
    @Override
    public String getUsername() {
        return this.username;
    } //getUsername

    /**
     * Returns the password of this user.
     *
     * @return the password of this user
     */
    @Override
    public String getPassword() {
        return this.password;
    } //getPassword

    /**
     * Returns the authorities of this user.
     *
     * @return the authorities of this user
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    } //getAuthorities

    /**
     * Returns whether this user's account is not expired.
     *
     * @return {@code true}, if this user's account is not expired and {@code false} otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    } //isAccountNonExpired

    /**
     * Returns whether this user's account is not locked.
     *
     * @return {@code true}, if this user's account is not locked and {@code false} otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    } //isAccountNonLocked

    /**
     * Returns whether this user's credentials are not expired.
     *
     * @return {@code true}, if this user's credentials are not expired and {@code false} otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    } //isCredentialsNonExpired

    /**
     * Returns whether this user's account is enabled.
     *
     * @return {@code true}, if this user's account is enabled and {@code false} otherwise
     */
    @Override
    public boolean isEnabled() {
        return true;
    } //isEnabled
}