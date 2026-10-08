package com.inventory.application.port.inbound;

import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;

public interface PublishInventorySecurityManifestUseCase {
    void execute(PublishInventorySecurityManifestCommand command);
}
