package com.identity.application.service;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.SecurityManifest;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.domain.port.outbound.SecurityManifestCatalogRepository;
import com.identity.domain.port.outbound.SecurityManifestInboxRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterSecurityManifestServiceTest {
    @Test
    void duplicateEventIsIgnoredAfterTheFirstCommittedRegistration() {
        var catalogCalls = new int[1];
        var inbox = new CapturingInbox();
        SecurityManifestCatalogRepository catalog = manifest -> catalogCalls[0]++;
        var service = new RegisterSecurityManifestService(inbox, catalog);
        var manifest = new SecurityManifest("merchant", 3,
                List.of(new ScopeDeclaration("merchant.account", null)),
                List.of(new AuthorityDefinition("MERCHANT_READ", "read", "view")));
        var command = new RegisterSecurityManifestCommand(manifest, "event-1");

        service.execute(command);
        service.execute(command);

        assertThat(catalogCalls[0]).isEqualTo(1);
        assertThat(inbox.eventId).isEqualTo("event-1");
    }

    private static final class CapturingInbox implements SecurityManifestInboxRepository {
        private String eventId;

        @Override
        public boolean alreadyProcessed(String eventId) {
            return this.eventId != null && this.eventId.equals(eventId);
        }

        @Override
        public void recordProcessed(String eventId, String moduleKey, int revision, String contentDigest) {
            this.eventId = eventId;
        }
    }
}
