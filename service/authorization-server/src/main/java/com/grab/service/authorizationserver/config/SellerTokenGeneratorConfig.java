package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.core.token.SellerJwtCustomizer;
import com.grab.service.authorizationserver.core.token.SellerSigningKeys;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

@Configuration(proxyBeanMethods = false)
public class SellerTokenGeneratorConfig {
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwks, SellerIssuerProperties properties) {
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        JOSEObjectType accessType = new JOSEObjectType("at+jwt");
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(accessType));
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwks));
        processor.setJWTClaimsSetVerifier((claims, context) -> {});
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(processor);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()), jwt -> {
                    boolean match = jwt.getExpiresAt() != null && jwt.getAudience() != null
                            && jwt.getAudience().contains(properties.audience());
                    if (match) {
                        return OAuth2TokenValidatorResult.success();
                    }
                    OAuth2Error invalid = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN);
                    return OAuth2TokenValidatorResult.failure(invalid);
                }));
        return decoder;
    }

    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(SellerSigningKeys keys, SellerIssuerProperties properties) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(keys.source());
        JwtGenerator jwt = new JwtGenerator(encoder);
        jwt.setJwtCustomizer(new SellerJwtCustomizer(keys, properties));
        return new DelegatingOAuth2TokenGenerator(jwt, new OAuth2AccessTokenGenerator(), new OAuth2RefreshTokenGenerator());
    }
}
