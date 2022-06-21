package com.coffee4j.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A web security configuration of the Coffee4j application.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version June 20, 2022
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
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
     * Configures web security using the specified authentication manager builder.
     *
     * @param auth the authentication manager builder to be used in the operation
     * @throws Exception if an error occurs
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsService service = new CustomUserDetailsService();

        auth.userDetailsService(service);
    } //configure

    /**
     * Configures web security using the specified HTTP security instance.
     *
     * @param http the HTTP security instance to be used in the operation
     * @throws Exception if an error occurs
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .cors()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/perform_login")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .formLogin()
            .loginProcessingUrl("/perform_login")
            .usernameParameter("username")
            .passwordParameter("password")
            .and()
            .logout()
            .deleteCookies("JSESSIONID");
    } //configure
}