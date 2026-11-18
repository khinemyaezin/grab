package com.grab.rpc.adapter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

final class MutualTlsRmiContexts {
    private MutualTlsRmiContexts() {}

    static SSLContext create(MutualTlsRmiEndpoint.SslMaterial material, boolean leafOnly) throws GeneralSecurityException, IOException {
        char[] keyPassword = material.keyPassword().toCharArray();
        char[] trustPassword = material.trustPassword().toCharArray();
        KeyStore keys = load(material.keyStore(), keyPassword);
        KeyStore peers = load(material.peerTrustStore(), trustPassword);
        Enumeration<String> aliases = peers.aliases();
        if (!aliases.hasMoreElements()) {
            throw new GeneralSecurityException("RMI peer allowlist is empty");
        }
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate certificate = peers.getCertificate(alias);
            if (!peers.isCertificateEntry(alias) || !(certificate instanceof X509Certificate peer)
                    || (leafOnly && peer.getBasicConstraints() >= 0)) {
                throw new GeneralSecurityException("RMI peer truststore must contain only workload leaf certificates");
            }
        }
        String keyAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(keyAlgorithm);
        keyManagers.init(keys, keyPassword);
        String trustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(trustAlgorithm);
        trustManagers.init(peers);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);
        return context;
    }

    private static KeyStore load(Path path, char[] password) throws GeneralSecurityException, IOException {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream stream = Files.newInputStream(path)) {
            store.load(stream, password);
        }
        return store;
    }

}
