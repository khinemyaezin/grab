package com.grab.discovery.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record RebuildDiscoveryProductsCommand() implements Command<Void> {
}
