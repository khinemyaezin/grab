package com.identity.domain.security;

public enum RoleDeclarationCandidateStatus {
    RECEIVED,
    WAITING_DEPENDENCY,
    APPLIED,
    SUPERSEDED,
    QUARANTINED
}
