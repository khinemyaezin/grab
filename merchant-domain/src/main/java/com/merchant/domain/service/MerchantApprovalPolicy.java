package com.merchant.domain.service;

import com.merchant.domain.aggregate.MerchantAccount;

public interface MerchantApprovalPolicy {
    boolean canAutoApprove(MerchantAccount merchant);
}
