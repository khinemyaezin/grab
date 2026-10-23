package com.saleschannel.domain.policy;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublishableKeyManagementPolicyTest {
    private final PublishableKeyManagementPolicy policy = new PublishableKeyManagementPolicy();
    private final Id channelId = new CommonId("channel-1");
    private final Id merchantId = new CommonId("merchant-1");

    @Test
    void requireAllowed_owningMerchant_allowsManagement() {
        PublishableKeyManagementAccess access = new PublishableKeyManagementAccess(merchantId, false);
        assertThatCode(() -> policy.requireAllowed(channelId, ChannelOwner.SELLER, merchantId, access))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAllowed_otherMerchant_rejectsManagement() {
        Id otherMerchantId = new CommonId("merchant-2");
        PublishableKeyManagementAccess access = new PublishableKeyManagementAccess(otherMerchantId, false);
        assertThatThrownBy(() -> policy.requireAllowed(channelId, ChannelOwner.SELLER, merchantId, access))
                .isInstanceOf(SalesChannelDomainException.class);
    }

    @Test
    void requireAllowed_platformChannel_rejectsMerchant() {
        PublishableKeyManagementAccess access = new PublishableKeyManagementAccess(merchantId, false);
        assertThatThrownBy(() -> policy.requireAllowed(channelId, ChannelOwner.PLATFORM, null, access))
                .isInstanceOf(SalesChannelDomainException.class);
    }

    @Test
    void requireAllowed_administrator_allowsAnyChannelWithoutMerchantScope() {
        PublishableKeyManagementAccess access = new PublishableKeyManagementAccess(null, true);
        assertThatCode(() -> policy.requireAllowed(channelId, ChannelOwner.PLATFORM, null, access))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.requireAllowed(channelId, ChannelOwner.SELLER, merchantId, access))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAllowed_missingAccessOrMerchant_rejectsManagement() {
        PublishableKeyManagementAccess access = new PublishableKeyManagementAccess(null, false);
        assertThatThrownBy(() -> policy.requireAllowed(channelId, ChannelOwner.SELLER, merchantId, access))
                .isInstanceOf(SalesChannelDomainException.class);
        assertThatThrownBy(() -> policy.requireAllowed(channelId, ChannelOwner.SELLER, merchantId, null))
                .isInstanceOf(SalesChannelDomainException.class);
    }
}
