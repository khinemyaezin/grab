package com.merchant.application.port.outbound;

import com.merchant.application.model.read.MerchantMemberView;

import java.util.List;
import java.util.Optional;

public interface MerchantMemberQueryPort {
    List<MerchantMemberView> findByMerchantId(String merchantId);

    Optional<MerchantMemberView> findById(String memberId);

    Optional<MerchantMemberView> findByMerchantIdAndUserId(String merchantId, String userId);
}
