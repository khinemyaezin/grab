package com.grab.framework.security;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record AuthorityManifest(
        String moduleKey,
        int version,
        List<AuthorityDefinition> definitions
) {
    public AuthorityManifest {
        Objects.requireNonNull(moduleKey, "module key is required");
        Objects.requireNonNull(definitions, "authority definitions are required");
        if (moduleKey.isBlank()) {
            throw new IllegalArgumentException("module key must not be blank");
        }
        if (version < 1) {
            throw new IllegalArgumentException("manifest version must be positive");
        }

        definitions = List.copyOf(definitions);
        var codes = new HashSet<String>();
        for (AuthorityDefinition definition : definitions) {
            Objects.requireNonNull(definition, "authority definition is required");
            if (!codes.add(definition.code())) {
                throw new IllegalArgumentException("duplicate authority code: " + definition.code());
            }
        }
    }

    /** Stable SHA-256 digest of the manifest definitions, independent of declaration order. */
    public String contentDigest() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            definitions.stream()
                    .sorted(java.util.Comparator.comparing(AuthorityDefinition::code))
                    .forEach(definition -> {
                        update(digest, definition.code());
                        update(digest, definition.name());
                        update(digest, definition.description());
                    });
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void update(MessageDigest digest, String value) {
        if (value == null) {
            digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(-1).array());
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
    }
}
