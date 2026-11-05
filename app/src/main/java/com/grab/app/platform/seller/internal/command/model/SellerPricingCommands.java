package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.framework.cqrs.command.Command;

import java.time.Instant;

public final class SellerPricingCommands {

    private SellerPricingCommands() {}

    public record CreateSellerPriceListCommand(
            String title,
            String description,
            String type,
            Instant startsAt,
            Instant endsAt
    ) implements Command<PriceListOutboundResult> {}

    public record CreateSellerPriceSetCommand() implements Command<PriceSetOutboundResult> {}

    public record CreateSellerPricePreferenceCommand(
            String attribute,
            String value,
            boolean taxInclusive
    ) implements Command<PricePreferenceOutboundResult> {}
}
