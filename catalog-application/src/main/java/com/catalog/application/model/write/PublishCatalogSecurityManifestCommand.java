package com.catalog.application.model.write;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.grab.framework.cqrs.command.Command;

public record PublishCatalogSecurityManifestCommand() implements Command<PublicationResult> {
}
