package com.saleschannel.domain.policy;

import com.grab.framework.id.Id;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.exception.SalesChannelDomainError;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;

public class PublishableKeyManagementPolicy {
    public void requireAllowed(Id salesChannelId, ChannelOwner owner, Id owningMerchantId,
                               PublishableKeyManagementAccess access) {
        if (access != null && access.administrator()) {
            return;
        }
        Id actingMerchantId = access == null ? null : access.merchantId();
        if (owner == ChannelOwner.SELLER && owningMerchantId != null
                && owningMerchantId.equals(actingMerchantId)) {
            return;
        }
        String channelId = salesChannelId.getValue();
        SalesChannelDomainError error = new SalesChannelDomainError.PublishableKeyManagementForbidden(channelId);
        throw new SalesChannelDomainException(error, "Publishable key management is forbidden");
    }
}
