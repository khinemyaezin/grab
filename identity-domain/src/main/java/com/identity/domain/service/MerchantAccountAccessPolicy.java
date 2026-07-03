package com.identity.domain.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.SessionStore;
import com.identity.domain.valueobject.AccessScope;

import java.util.Objects;
import java.util.Optional;

public final class MerchantAccountAccessPolicy {
    private final AccessAssignmentRepository assignments;
    private final SessionStore sessions;
    private final IdGenerator ids;

    public MerchantAccountAccessPolicy(
            AccessAssignmentRepository assignments,
            SessionStore sessions,
            IdGenerator ids
    ) {
        this.assignments = Objects.requireNonNull(assignments, "access assignment repository is required");
        this.sessions = Objects.requireNonNull(sessions, "session store is required");
        this.ids = Objects.requireNonNull(ids, "ID generator is required");
    }

    public AccessAssignment replaceApplicantWithOwner(
            Id applicantUserId,
            Id merchantId,
            Platform sellerPlatform
    ) {
        Objects.requireNonNull(applicantUserId, "applicant user ID is required");
        Objects.requireNonNull(merchantId, "merchant ID is required");
        Objects.requireNonNull(sellerPlatform, "seller platform is required");

        sellerPlatform.requireSupportedRole(MerchantAccessProfile.APPLICANT_ROLE_CODE);
        sellerPlatform.requireSupportedRole(MerchantAccessProfile.OWNER_ROLE_CODE);

        AccessScope merchantScope = AccessScope.from(
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                merchantId.getValue()
        );
        revokeApplicantAccess(applicantUserId, merchantScope);

        Optional<AccessAssignment> currentOwner = assignments.findCurrent(
                applicantUserId,
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.OWNER_ROLE_CODE,
                merchantScope
        );
        if (currentOwner.isPresent()) {
            return currentOwner.get();
        }

        Id ownerAssignmentId = ids.generateId();
        AccessAssignment owner = AccessAssignment.create(
                ownerAssignmentId,
                applicantUserId,
                sellerPlatform,
                MerchantAccessProfile.OWNER_ROLE_CODE,
                merchantScope,
                null,
                null
        );
        return assignments.save(owner);
    }

    private void revokeApplicantAccess(Id applicantUserId, AccessScope merchantScope) {
        Optional<AccessAssignment> currentApplicant = assignments.findCurrent(
                applicantUserId,
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.APPLICANT_ROLE_CODE,
                merchantScope
        );
        if (currentApplicant.isEmpty()) {
            return;
        }

        AccessAssignment applicant = currentApplicant.get();
        applicant.revoke();
        AccessAssignment revokedApplicant = assignments.save(applicant);
        sessions.revokeByAssignment(revokedApplicant.getId().getValue());
    }
}
