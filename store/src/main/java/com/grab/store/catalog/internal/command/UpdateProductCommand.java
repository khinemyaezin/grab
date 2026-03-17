package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdateProductCommand(
        Id productId,
        String name,
        Id categoryId,
        Id sellerId,
        String sellerType,
        String condition,
        Boolean offerEligible,
        String slug,
        Boolean featured,
        String moderationNote
) implements Command<UpdateProductResult> {}
