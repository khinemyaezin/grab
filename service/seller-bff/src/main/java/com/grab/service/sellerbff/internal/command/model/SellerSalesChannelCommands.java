package com.grab.service.sellerbff.internal.command.model;

import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ProvisionOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.PublishableKeyOutboundResult;
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

