package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

public record SaveCategoryCommand(
        String name,
        Id parentId
) implements Command<SaveCategoryResult> {
}
