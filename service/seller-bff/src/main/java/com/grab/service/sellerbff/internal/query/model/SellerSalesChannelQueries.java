package com.grab.service.sellerbff.internal.query.model;

import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.KeyResolutionOutboundResult;
import com.grab.framework.cqrs.query.Query;

import java.util.List;
import java.util.Optional;

public final class SellerSalesChannelQueries {

    private SellerSalesChannelQueries() {}

    public record GetSellerSalesChannelQuery(
            String salesChannelId,
            String merchantId
    ) implements Query<Optional<ChannelOutboundResult>> {}

    public record ListSellerSalesChannelsQuery(
            String merchantId,
            int page,
            int size
    ) implements Query<List<ChannelOutboundResult>> {}

    public record ResolveSellerPublishableKeyQuery(
            String rawToken
    ) implements Query<Optional<KeyResolutionOutboundResult>> {}
}

