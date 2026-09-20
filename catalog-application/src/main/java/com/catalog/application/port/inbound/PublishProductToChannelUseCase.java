package com.catalog.application.port.inbound;

import com.catalog.application.command.PublishProductToChannelCommand;
import com.catalog.application.command.PublishProductToChannelResult;

public interface PublishProductToChannelUseCase {
    PublishProductToChannelResult execute(PublishProductToChannelCommand command);
}
