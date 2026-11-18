package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.core.token.SellerSigningKeys;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration(proxyBeanMethods = false)
public class SellerSigningKeysConfig {
    @Bean
    public SellerSigningKeys sellerSigningKeys(SellerIssuerProperties properties)
            throws GeneralSecurityException, IOException {
        return SellerSigningKeys.load(properties);
    }

    @Bean
    public JWKSource<SecurityContext> jwks(SellerSigningKeys signingKeys) {
        return signingKeys.source();
    }
}
