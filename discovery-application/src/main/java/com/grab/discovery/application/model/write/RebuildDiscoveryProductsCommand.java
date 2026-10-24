package com.grab.discovery.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record RebuildDiscoveryProductsCommand(int batchSize) implements Command<RebuildProgressResult> {
}
