package com.merchant.domain.policy;

import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

import java.util.Objects;

public class MerchantOwnershipPolicy {

    public void requireNotSoleAdmin(MerchantMember member, long activeAdminCount) {
        Objects.requireNonNull(member, "member is required");

        if (!member.isAdmin() || !member.getStatus().isActive()) {
            return;
        }

        if (activeAdminCount <= 1) {
            throw new MerchantDomainException(
                    new MerchantDomainError.CannotDemoteSoleAdmin(
                            member.getMerchantId().getValue(),
                            member.getId().getValue()
                    ),
                    "Cannot demote or remove the sole active admin of merchant " + member.getMerchantId().getValue()
            );
        }
    }
}
