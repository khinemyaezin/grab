package com.identity.domain.security;

/** Durable outcome of a complete manifest candidate. */
public enum SecurityManifestCandidateStatus {
    RECEIVED,
    WAITING_DEPENDENCY,
    APPLIED,
    SUPERSEDED,
    QUARANTINED
}
