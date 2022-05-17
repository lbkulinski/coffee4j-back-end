package com.coffee4j.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * A security configuration of the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version May 17, 2022
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    /**
     * Returns an instance of {@link BCryptPasswordEncoder}.
     *
     * @return an instance of {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    } //passwordEncoder

    /**
     * Returns a {@link SecurityFilterChain} using the specified {@link HttpSecurity} instance.
     *
     * @param http the {@link HttpSecurity} instance to be used in the operation
     * @return a {@link SecurityFilterChain} using the specified {@link HttpSecurity} instance
     * @throws Exception if an error occurs during the operation
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        UserDetailsService service = new CustomUserDetailsService();

        http.userDetailsService(service)
            .csrf()
            .disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/user")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .formLogin()
            .and()
            .logout();

        return http.build();
    } //filterChain
}