package com.catalog.application.port.inbound;

import com.catalog.application.model.write.UnpublishProductFromChannelCommand;
import com.catalog.application.model.write.UnpublishProductFromChannelResult;

public interface UnpublishProductFromChannelUseCase {
    UnpublishProductFromChannelResult execute(UnpublishProductFromChannelCommand command);
}
