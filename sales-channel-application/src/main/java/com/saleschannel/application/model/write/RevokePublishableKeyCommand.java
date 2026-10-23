package com.saleschannel.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;

import java.util.Objects;

public record RevokePublishableKeyCommand(Id salesChannelId, Id keyId, PublishableKeyManagementAccess access)
        implements Command<Void> {
    public RevokePublishableKeyCommand {
        Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        Objects.requireNonNull(keyId, "keyId is required");
    }
}
