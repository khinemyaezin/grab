package com.catalog.application.port.inbound;

import com.catalog.application.model.write.PublishProductToChannelCommand;
import com.catalog.application.model.write.PublishProductToChannelResult;

public interface PublishProductToChannelUseCase {
    PublishProductToChannelResult execute(PublishProductToChannelCommand command);
}
