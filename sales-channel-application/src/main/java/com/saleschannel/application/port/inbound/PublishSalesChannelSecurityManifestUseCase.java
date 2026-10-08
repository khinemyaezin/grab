package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;

public interface PublishSalesChannelSecurityManifestUseCase {
    void execute(PublishSalesChannelSecurityManifestCommand command);
}
