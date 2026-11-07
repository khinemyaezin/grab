package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.ProvisionOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.PublishableKeyOutboundResult;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public final class SellerSalesChannelCommands {

    private SellerSalesChannelCommands() {}

    public record ProvisionSellerMerchantWebsiteChannelCommand(
            Id merchantId,
            String countryCode
    ) implements Command<ProvisionOutboundResult> {}

    public record CreateSellerPublishableKeyCommand(
            Id salesChannelId,
            String label
    ) implements Command<PublishableKeyOutboundResult> {}

    public record RevokeSellerPublishableKeyCommand(
            Id salesChannelId,
            Id keyId
    ) implements Command<Void> {}
}

