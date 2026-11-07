package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.CreateVariantRecord;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;

public final class SellerProductCommands {

    private SellerProductCommands() {}

    public record CreateSellerProductCommand(
            Id merchantId,
            String name,
            Id categoryId,
            String condition,
            String slug,
            String status,
            List<CreateVariantRecord> variants
    ) implements Command<ProductOutboundResult> {}

    public record UpdateSellerProductCommand(
            Id merchantId,
            Id productId,
            String name,
            Id categoryId,
            String condition,
            String slug
    ) implements Command<ProductOutboundResult> {}

    public record DeleteSellerProductCommand(
            Id merchantId,
            Id productId
    ) implements Command<Void> {}

    public record UpdateSellerProductStatusCommand(
            Id merchantId,
            Id productId,
            String status
    ) implements Command<ProductOutboundResult> {}
}

