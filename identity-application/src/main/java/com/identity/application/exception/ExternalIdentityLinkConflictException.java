package com.identity.application.exception;

public final class ExternalIdentityLinkConflictException extends RuntimeException {
    public ExternalIdentityLinkConflictException() {
        super("External identity is already linked to a different platform user or directory resource");
    }
}
