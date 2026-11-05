package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.framework.cqrs.command.Command;

public final class SellerCategoryCommands {

    private SellerCategoryCommands() {}

    public record SaveSellerCategoryCommand(
            String name,
            String parentId,
            Boolean active,
            Boolean listingAllowed,
            Boolean c2cAllowed
    ) implements Command<CategoryOutboundResult> {}

    public record DeleteSellerCategoryCommand(
            String categoryId
    ) implements Command<Void> {}
}
