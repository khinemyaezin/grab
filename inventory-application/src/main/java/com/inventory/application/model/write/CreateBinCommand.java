package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record CreateBinCommand(
        Id zoneId,
        String code,
        String name,
        Integer maxCapacity,
        String actorId,
        String scopeKey,
        String scopeId
) implements Command<BinResult> {
}
