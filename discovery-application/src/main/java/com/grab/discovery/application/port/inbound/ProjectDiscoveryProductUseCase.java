package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;

public interface ProjectDiscoveryProductUseCase {
    void execute(ProjectDiscoveryProductCommand command);
}
