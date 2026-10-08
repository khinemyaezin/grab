package com.merchant.domain.port.outbound;

import com.grab.framework.id.Id;
import com.merchant.domain.aggregate.MerchantMember;

import java.util.Optional;

public interface MerchantMemberRepository {
    Optional<MerchantMember> findById(Id id);

    Optional<MerchantMember> findByMerchantIdAndUserId(Id merchantId, Id userId);

    long countActiveAdmins(Id merchantId);

    boolean existsByMerchantIdAndUserId(Id merchantId, Id userId);

    MerchantMember save(MerchantMember member);
}
