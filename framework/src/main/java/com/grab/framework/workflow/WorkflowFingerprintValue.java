package com.grab.framework.workflow;

import java.util.Objects;

public record WorkflowFingerprintValue(int algorithmVersion, String digest) {

    public WorkflowFingerprintValue {
        if (algorithmVersion < 1) {
            throw new IllegalArgumentException("Fingerprint version must be positive");
        }
        Objects.requireNonNull(digest, "digest");
        if (digest.isBlank()) {
            throw new IllegalArgumentException("Fingerprint digest must not be blank");
        }
    }
}
