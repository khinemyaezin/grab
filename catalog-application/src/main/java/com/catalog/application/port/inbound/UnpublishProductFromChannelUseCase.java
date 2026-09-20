package com.catalog.application.port.inbound;

import com.catalog.application.command.UnpublishProductFromChannelCommand;
import com.catalog.application.command.UnpublishProductFromChannelResult;

public interface UnpublishProductFromChannelUseCase {
    UnpublishProductFromChannelResult execute(UnpublishProductFromChannelCommand command);
}
