package com.merchant.domain.policy.impl;

import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.policy.MerchantApprovalPolicy;

public final class SystemDefaultMerchantApprovalPolicy implements MerchantApprovalPolicy {

    public SystemDefaultMerchantApprovalPolicy() {
    }

    @Override
    public boolean canAutoApprove(MerchantAccount merchant) {
        if (merchant == null || merchant.getType() == null) {
            return false;
        }
        
        return true;
    }
}
