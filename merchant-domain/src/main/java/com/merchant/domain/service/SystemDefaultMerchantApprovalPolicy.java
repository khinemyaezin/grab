package com.merchant.domain.service;

import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;

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
