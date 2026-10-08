package com.catalog.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record PublishCatalogSecurityManifestCommand() implements Command<Void> {
}
