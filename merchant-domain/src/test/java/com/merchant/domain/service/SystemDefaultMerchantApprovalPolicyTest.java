package com.merchant.domain.service;

import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemDefaultMerchantApprovalPolicyTest {

    private final MerchantApprovalPolicy policy = new SystemDefaultMerchantApprovalPolicy();

    @Test
    void canAutoApprove_c2cSellerMerchant_returnsTrue() {
        MerchantAccount merchant = mock(MerchantAccount.class);
        when(merchant.getType()).thenReturn(MerchantType.C2C_SELLER);

        assertTrue(policy.canAutoApprove(merchant));
    }

    @Test
    void canAutoApprove_nullMerchant_returnsFalse() {
        assertFalse(policy.canAutoApprove(null));
    }

    @Test
    void canAutoApprove_nullMerchantType_returnsFalse() {
        MerchantAccount merchant = mock(MerchantAccount.class);
        when(merchant.getType()).thenReturn(null);

        assertFalse(policy.canAutoApprove(merchant));
    }
}
