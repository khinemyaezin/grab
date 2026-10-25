package com.grab.app.channeldiscovery.saleschannel.internal.adapter;

import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecurePublishableTokenGenerator implements PublishableTokenGeneratorPort {
    private static final String PREFIX = "pk_";
    private final SecureRandom secureRandom;

    public SecurePublishableTokenGenerator() {
        this.secureRandom = new SecureRandom();
    }

    @Override
    public GeneratedToken generate() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String rawToken = PREFIX + payload;
        String digest = digest(rawToken);
        String displayPrefix = PREFIX + payload.substring(0, 8);
        return new GeneratedToken(rawToken, digest, displayPrefix);
    }

    @Override
    public String digest(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
