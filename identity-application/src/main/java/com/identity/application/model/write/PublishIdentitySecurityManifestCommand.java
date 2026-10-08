package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record PublishIdentitySecurityManifestCommand() implements Command<Void> {
}
