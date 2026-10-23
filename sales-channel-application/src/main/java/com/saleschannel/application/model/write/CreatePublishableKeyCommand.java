package com.saleschannel.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;

import java.util.Objects;

public record CreatePublishableKeyCommand(Id salesChannelId, String label, PublishableKeyManagementAccess access)
        implements Command<CreatePublishableKeyResult> {
    public CreatePublishableKeyCommand {
        Objects.requireNonNull(salesChannelId, "salesChannelId is required");
    }
}
