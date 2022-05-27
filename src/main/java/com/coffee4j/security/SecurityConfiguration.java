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
 * @version May 27, 2022
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