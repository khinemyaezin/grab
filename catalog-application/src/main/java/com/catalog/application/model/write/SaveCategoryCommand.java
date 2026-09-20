package com.catalog.application.model.write;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

public record SaveCategoryCommand(
        String name,
        Id parentId,
        Boolean active,
        Boolean listingAllowed,
        Boolean c2cAllowed
) implements Command<SaveCategoryResult> {
}
