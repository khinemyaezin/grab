package com.saleschannel.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;

public interface PublishSalesChannelSecurityManifestUseCase {
    PublicationResult execute(PublishSalesChannelSecurityManifestCommand command);
}
