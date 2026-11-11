package com.grab.app.account.identity.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LocalJwtProperties.class)
public class IdentityKeyConfig {

    @Bean
    public KeyPair localJwtKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot initialize JWT keys", ex);
        }
    }

    @Bean
    public PublicKey localJwtPublicKey(KeyPair pair) {
        return pair.getPublic();
    }

    @Bean
    public PrivateKey localJwtPrivateKey(KeyPair pair) {
        return pair.getPrivate();
    }
}

