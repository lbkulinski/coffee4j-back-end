package com.coffee4j.security;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Value("{auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        String pattern = "/api/**";

        httpSecurity.authorizeRequests()
                    .mvcMatchers(HttpMethod.GET, pattern)
                    .permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .oauth2ResourceServer()
                    .jwt();
    } //configure

    public JwtDecoder jwtDecoder() {
        OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(this.audience);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(this.issuer);

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<Jwt>(withAudience, withIssuer);

        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    } //jwtDecoder
}