package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DiscontinueInventoryForDeletedVariantCommand(
        Id productVariantId
) implements Command<DiscontinueInventoryForDeletedVariantResult> {
}
