package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record PublishInventorySecurityManifestCommand() implements Command<Void> {
}
