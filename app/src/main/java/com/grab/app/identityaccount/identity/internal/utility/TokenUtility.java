package com.grab.app.identityaccount.identity.internal.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class TokenUtility {
    private String hash(String token) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
