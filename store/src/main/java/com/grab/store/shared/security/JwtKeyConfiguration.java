package com.grab.store.shared.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.security.*;

@Configuration
@EnableConfigurationProperties(LocalJwtProperties.class)
public class JwtKeyConfiguration {
    @Bean
    KeyPair localJwtKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot initialize JWT keys", ex);
        }
    }

    @Bean
    PublicKey localJwtPublicKey(KeyPair pair) {
        return pair.getPublic();
    }

    @Bean
    PrivateKey localJwtPrivateKey(KeyPair pair) {
        return pair.getPrivate();
    }
}
