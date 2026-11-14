package com.grab.app.shared.bootstrap;

import com.grab.framework.bootstrap.BootstrapTaskException;
import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicationBootstrapOutcomeTest {

    @Test
    void enqueuedNotDueAndSuperseded_satisfyTheTask() {
        assertDoesNotThrow(() -> PublicationBootstrapOutcome.require("catalog", PublicationResult.ENQUEUED));
        assertDoesNotThrow(() -> PublicationBootstrapOutcome.require("catalog", PublicationResult.NOT_DUE));
        assertDoesNotThrow(() -> PublicationBootstrapOutcome.require("catalog", PublicationResult.SUPERSEDED));
    }

    @Test
    void conflict_failsPermanently() {
        BootstrapTaskException manifest = assertThrows(BootstrapTaskException.class,
                () -> PublicationBootstrapOutcome.require("catalog", PublicationResult.CONFLICT));
        BootstrapTaskException role = assertThrows(BootstrapTaskException.class,
                () -> PublicationBootstrapOutcome.require("merchant.role-declaration",
                        RoleDeclarationPublicationPort.PublicationResult.CONFLICT));

        assertTrue(manifest.isPermanent());
        assertTrue(role.isPermanent());
    }

    @Test
    void missingResult_isRetryable() {
        PublicationResult missing = null;

        BootstrapTaskException error = assertThrows(BootstrapTaskException.class,
                () -> PublicationBootstrapOutcome.require("catalog", missing));

        assertFalse(error.isPermanent());
    }
}
