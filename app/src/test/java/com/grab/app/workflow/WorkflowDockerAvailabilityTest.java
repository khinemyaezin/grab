package com.grab.app.workflow;

import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowDockerAvailabilityTest {

    @Test
    void verificationRequiresDockerWhenExplicitlyEnabled() {
        if (!Boolean.getBoolean("workflow.testcontainers.required")) {
            return;
        }
        assertTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required for workflow verification");
    }
}
