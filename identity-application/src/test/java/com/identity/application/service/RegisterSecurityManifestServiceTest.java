package com.identity.application.service;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.*;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.port.outbound.*;
import com.identity.domain.security.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterSecurityManifestServiceTest {
    @Test
    void execute_duplicateAndSemanticReplay_doNotRepeatActivation() {
        var storage = new Storage();
        var service = new RegisterSecurityManifestService(storage, storage, storage);
        var manifest = merchant("Read");
        var command = new RegisterSecurityManifestCommand(manifest, "event-1");
        assertThat(service.execute(command).newlyActivated()).isTrue();
        assertThat(service.execute(command).newlyActivated()).isFalse();
        storage.receipts.clear();
        assertThat(service.execute(new RegisterSecurityManifestCommand(manifest, "event-2")).newlyActivated()).isFalse();
        assertThat(storage.activations).isEqualTo(1);
        assertThat(storage.candidates.get("merchant:2").eventId()).isEqualTo("event-1");
    }

    @Test
    void execute_conflictingDelivery_preservesReceiptAndCanonicalPayload() {
        var storage = new Storage();
        var service = new RegisterSecurityManifestService(storage, storage, storage);
        var original = merchant("Read");
        service.execute(new RegisterSecurityManifestCommand(original, "event-1"));
        var collision = merchant("Changed");
        var result = service.execute(new RegisterSecurityManifestCommand(collision, "event-1"));
        assertThat(result.outcome()).isEqualTo("QUARANTINED");
        assertThat(storage.conflicts).isEqualTo(1);
        assertThat(storage.receipts.get("event-1").status()).isEqualTo(SecurityManifestCandidateStatus.APPLIED);
        assertThat(storage.candidates.get("merchant:2").manifest()).isEqualTo(original);
        assertThat(storage.activations).isEqualTo(1);
    }

    private SecurityManifest merchant(String name) {
        return new SecurityManifest("merchant", 2, List.of(new ScopeDeclaration("merchant.account", null)),
                List.of(new AuthorityDefinition("MERCHANT_READ", name, null)));
    }

    private static class Storage implements SecurityCatalogRepository, SecurityManifestRevisionRepository, SecurityManifestInboxRepository {
        private final SecurityCatalog catalog = SecurityCatalog.rehydrate(new CommonId("catalog"), 0, List.of(), List.of(), List.of());
        private final Map<String, SecurityManifestCandidate> candidates = new HashMap<>();
        private final Map<String, SecurityManifestReceipt> receipts = new HashMap<>();
        private int activations;
        private int conflicts;

        @Override
        public SecurityCatalog loadForUpdate() { return catalog; }
        @Override
        public void save(SecurityCatalog catalog) { activations++; }
        @Override
        public Optional<SecurityManifestCandidate> find(String module, int revision) { return Optional.ofNullable(candidates.get(module + ":" + revision)); }
        @Override
        public int highestAcceptedRevision(String module) {
            return candidates.values().stream().filter(c -> c.manifest().moduleKey().equals(module))
                    .filter(c -> c.status() == SecurityManifestCandidateStatus.APPLIED || c.status() == SecurityManifestCandidateStatus.WAITING_DEPENDENCY)
                    .mapToInt(c -> c.manifest().securityRevision()).max().orElse(0);
        }
        @Override
        public void save(SecurityManifestCandidate candidate) { candidates.put(candidate.manifest().moduleKey() + ":" + candidate.manifest().securityRevision(), candidate); }
        @Override
        public Optional<SecurityManifestReceipt> find(String event) { return Optional.ofNullable(receipts.get(event)); }
        @Override
        public void save(SecurityManifestReceipt receipt) { receipts.put(receipt.eventId(), receipt); }
        @Override
        public void recordConflict(String event, SecurityManifest manifest, String supplied, String error) { conflicts++; }
    }
}
