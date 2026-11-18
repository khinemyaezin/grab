package com.grab.service.account.identity.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LocalJwtProperties.class)
public class IdentityKeyConfig {

    @Bean
    @Profile("!prod")
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
    @Profile("prod")
    public KeyPair productionJwtKeyPair(
            @Value("${security.local-jwt.keystore-path}") String keyStorePath,
            @Value("${security.local-jwt.keystore-password}") String keyStorePassword,
            @Value("${security.local-jwt.key-alias:account-service}") String keyAlias) {
        try (InputStream input = Files.newInputStream(Path.of(keyStorePath))) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(input, keyStorePassword.toCharArray());
            Key key = keyStore.getKey(keyAlias, keyStorePassword.toCharArray());
            if (!(key instanceof PrivateKey privateKey)) {
                throw new IllegalStateException("JWT keystore alias must contain a private key: " + keyAlias);
            }
            PublicKey publicKey = keyStore.getCertificate(keyAlias).getPublicKey();
            if (!"RSA".equalsIgnoreCase(privateKey.getAlgorithm())
                    || !"RSA".equalsIgnoreCase(publicKey.getAlgorithm())) {
                throw new IllegalStateException("JWT keystore alias must contain an RSA key pair: " + keyAlias);
            }
            return new KeyPair(publicKey, privateKey);
        } catch (GeneralSecurityException | java.io.IOException exception) {
            throw new IllegalStateException("Unable to load production JWT signing key", exception);
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

