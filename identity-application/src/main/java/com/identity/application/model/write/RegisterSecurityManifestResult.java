package com.identity.application.model.write;

public record RegisterSecurityManifestResult(String moduleKey, int securityRevision, String contentDigest,
                                            String outcome, String errorCode, boolean newlyActivated) {
}
