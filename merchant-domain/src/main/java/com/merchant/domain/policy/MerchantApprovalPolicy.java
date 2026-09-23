package com.merchant.domain.policy;

import com.merchant.domain.aggregate.MerchantAccount;

public interface MerchantApprovalPolicy {
    boolean canAutoApprove(MerchantAccount merchant);
}
