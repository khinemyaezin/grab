package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record RevalidateSecurityManifestCommand(String moduleKey, int securityRevision)
        implements Command<RegisterSecurityManifestResult> {
}
