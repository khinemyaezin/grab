package com.identity.domain.port.outbound;

import com.identity.domain.security.RoleDeclarationCandidate;
import com.identity.domain.security.RoleDeclarationReceipt;
import com.identity.domain.security.RoleDeclarationState;

import java.util.Optional;

public interface RoleDeclarationRepository {
    Optional<RoleDeclarationCandidate> findCandidate(String owner, String roleCode, int revision);

    int highestAcceptedRevision(String owner, String roleCode);

    void saveCandidate(RoleDeclarationCandidate candidate);

    Optional<RoleDeclarationReceipt> findReceipt(String eventId);

    void saveReceipt(RoleDeclarationReceipt receipt);

    Optional<RoleDeclarationState> findState(String roleCode);

    void saveState(RoleDeclarationState state);

    void recordConflict(
            String eventId,
            String owner,
            String roleCode,
            int revision,
            String suppliedDigest,
            String existingDigest,
            String payload,
            String reason
    );
}
