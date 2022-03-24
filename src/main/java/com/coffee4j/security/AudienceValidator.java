package com.coffee4j.security;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

public final class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;

    public AudienceValidator(String audience) {
        Assert.hasText(audience, "The specified audience is null or empty");

        this.audience = audience;
    } //AudienceValidator

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audiences = jwt.getAudience();

        if (audiences.contains(this.audience)) {
            return OAuth2TokenValidatorResult.success();
        } //end if

        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN);

        return OAuth2TokenValidatorResult.failure(error);
    } //validate
}