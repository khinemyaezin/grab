package com.saleschannel.domain.service;

import com.grab.framework.id.Id;
import com.saleschannel.domain.exception.SalesChannelDomainError;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import com.saleschannel.domain.repository.SalesChannelRepository;

import java.util.Objects;

public final class SalesChannelUniquenessService {
    private final SalesChannelRepository salesChannels;

    public SalesChannelUniquenessService(SalesChannelRepository salesChannels) {
        this.salesChannels = Objects.requireNonNull(salesChannels, "sales channel repository is required");
    }

    public void requireWebsiteAvailable(Id merchantId) {
        if (salesChannels.existsWebsiteByMerchantId(merchantId)) {
            throw new SalesChannelDomainException(
                    new SalesChannelDomainError.DuplicateWebsite(merchantId.getValue()),
                    "A website channel already exists for this merchant"
            );
        }
    }

    public void requireMarketplaceAvailable() {
        if (salesChannels.existsMarketplace()) {
            throw new SalesChannelDomainException(
                    new SalesChannelDomainError.DuplicateMarketplace(),
                    "A marketplace channel already exists"
            );
        }
    }
}
