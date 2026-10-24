package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;

public interface RebuildDiscoveryProductsUseCase {
    RebuildProgressResult execute(RebuildDiscoveryProductsCommand command);
}
