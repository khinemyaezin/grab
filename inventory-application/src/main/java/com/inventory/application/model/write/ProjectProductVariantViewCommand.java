package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ProjectProductVariantViewCommand(Update update) implements Command<ProjectProductVariantViewResult> {
    public sealed interface Update permits UpsertVariant, SetVariantStatus, RenameProduct, DeleteProduct {
    }

    public record UpsertVariant(
            Id productId, Id variantId, String sku, String productName,
            boolean manageInventory, boolean replacePresentation
    ) implements Update {
    }

    public record SetVariantStatus(Id variantId, boolean active) implements Update {
    }

    public record RenameProduct(Id productId, String productName) implements Update {
    }

    public record DeleteProduct(Id productId) implements Update {
    }
}
