package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;

public interface RebuildDiscoveryProductsUseCase {
    void execute(RebuildDiscoveryProductsCommand command);
}
