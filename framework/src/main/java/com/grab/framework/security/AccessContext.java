package com.grab.framework.security;

import java.io.Serializable;

public record AccessContext(
        String assignmentId,
        String scopeKey,
        String scopeId
) implements Serializable {
}
