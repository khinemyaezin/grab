package com.catalog.application.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

public record UpdateProductStatusCommand(
        Id merchantId,
        Id productId,
        String status
) implements Command<UpdateProductStatusResult> {}
