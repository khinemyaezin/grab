package com.grab.service.authorizationserver;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

record SellerSigningKeys(String currentId, JWKSource<SecurityContext> source) {
    static SellerSigningKeys load(SellerIssuerProperties properties) throws GeneralSecurityException, IOException {
        KeyStore store = KeyStore.getInstance("PKCS12");
        char[] password = properties.keyStorePassword().toCharArray();
        try (InputStream stream = Files.newInputStream(properties.keyStore())) {
            store.load(stream, password);
        }
        String alias = properties.signingAlias();
        Key key = store.getKey(alias, password);
        if (!(key instanceof RSAPrivateKey privateKey)) {
            throw new GeneralSecurityException("Missing RSA signing key");
        }
        RSAKey current = verificationKey(store, alias).privateKey(privateKey).build();
        List<JWK> verification = new ArrayList<>();
        verification.add(current);
        for (String retiring : properties.retiringAliases()) {
            if (!retiring.isBlank() && !retiring.equals(alias)) {
                RSAKey retiringKey = verificationKey(store, retiring).build();
                verification.add(retiringKey);
            }
        }
        JWKSet keys = new JWKSet(verification);
        return new SellerSigningKeys(alias, (selector, context) -> selector.select(keys));
    }

    private static RSAKey.Builder verificationKey(KeyStore store, String alias) throws GeneralSecurityException {
        Certificate certificate = store.getCertificate(alias);
        if (certificate == null || !(certificate.getPublicKey() instanceof RSAPublicKey rsa)
                || rsa.getModulus().bitLength() < 2048) {
            throw new GeneralSecurityException("Signing certificate must contain an RSA key of at least 2048 bits");
        }
        return new RSAKey.Builder(rsa).keyID(alias).keyUse(KeyUse.SIGNATURE).algorithm(JWSAlgorithm.RS256);
    }
}
