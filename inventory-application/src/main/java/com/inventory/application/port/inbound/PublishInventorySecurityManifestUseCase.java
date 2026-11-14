package com.inventory.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;

public interface PublishInventorySecurityManifestUseCase {
    PublicationResult execute(PublishInventorySecurityManifestCommand command);
}
