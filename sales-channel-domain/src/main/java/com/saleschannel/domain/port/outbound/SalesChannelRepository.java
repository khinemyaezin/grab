package com.saleschannel.domain.port.outbound;

import com.grab.framework.id.Id;
import com.saleschannel.domain.aggregate.SalesChannel;

import java.util.Optional;

public interface SalesChannelRepository {
    Optional<SalesChannel> findById(Id id);

    Optional<SalesChannel> findWebsiteByMerchantId(Id merchantId);

    Optional<SalesChannel> findMarketplace();

    boolean existsWebsiteByMerchantId(Id merchantId);

    boolean existsMarketplace();

    SalesChannel save(SalesChannel salesChannel);
}
