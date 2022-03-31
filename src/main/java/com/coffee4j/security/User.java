package com.coffee4j.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record User(String id, String username, String password) implements UserDetails {
    public User {
        Objects.requireNonNull(id, "the specified ID is null");

        Objects.requireNonNull(username, "the specified username is null");

        Objects.requireNonNull(password, "the specified password is null");
    } //User

    @Override
    public String getUsername() {
        return this.username;
    } //getUsername

    @Override
    public String getPassword() {
        return this.password;
    } //getPassword

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    } //getAuthorities

    @Override
    public boolean isAccountNonExpired() {
        return true;
    } //isAccountNonExpired

    @Override
    public boolean isAccountNonLocked() {
        return true;
    } //isAccountNonLocked

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    } //isCredentialsNonExpired

    @Override
    public boolean isEnabled() {
        return true;
    } //isEnabled
}