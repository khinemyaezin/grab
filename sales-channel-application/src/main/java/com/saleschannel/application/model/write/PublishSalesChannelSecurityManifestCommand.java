package com.saleschannel.application.model.write;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.grab.framework.cqrs.command.Command;

public record PublishSalesChannelSecurityManifestCommand() implements Command<PublicationResult> {
}
