package com.identity.application.service;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.*;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;
import com.identity.application.model.write.RevalidateSecurityManifestCommand;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.port.outbound.*;
import com.identity.domain.security.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterSecurityManifestServiceTest {
    private static final UUID FIRST_DELIVERY = UUID.randomUUID();
    private static final UUID SECOND_DELIVERY = UUID.randomUUID();
    @Test
    void execute_duplicateAndSemanticReplay_doNotRepeatActivation() {
        var storage = new Storage();
        AtomicReference<UUID> deliveryId = new AtomicReference<>(FIRST_DELIVERY);
        RegisterSecurityManifestService service = new RegisterSecurityManifestService(
                storage, storage, storage, deliveryId::get);
        var manifest = merchant("Read");
        var command = new RegisterSecurityManifestCommand(manifest);
        assertThat(service.execute(command).newlyActivated()).isTrue();
        assertThat(service.execute(command).newlyActivated()).isFalse();
        storage.receipts.clear();
        deliveryId.set(SECOND_DELIVERY);
        assertThat(service.execute(new RegisterSecurityManifestCommand(manifest)).newlyActivated()).isFalse();
        assertThat(storage.activations).isEqualTo(1);
        assertThat(storage.candidates.get("merchant:2").eventId()).isEqualTo(FIRST_DELIVERY.toString());
    }

    @Test
    void execute_conflictingDelivery_preservesReceiptAndCanonicalPayload() {
        var storage = new Storage();
        AtomicReference<UUID> deliveryId = new AtomicReference<>(FIRST_DELIVERY);
        RegisterSecurityManifestService service = new RegisterSecurityManifestService(
                storage, storage, storage, deliveryId::get);
        var original = merchant("Read");
        service.execute(new RegisterSecurityManifestCommand(original));
        var collision = merchant("Changed");
        var result = service.execute(new RegisterSecurityManifestCommand(collision));
        assertThat(result.outcome()).isEqualTo("QUARANTINED");
        assertThat(storage.conflicts).isEqualTo(1);
        assertThat(storage.receipts.get(FIRST_DELIVERY.toString()).status()).isEqualTo(SecurityManifestCandidateStatus.APPLIED);
        assertThat(storage.candidates.get("merchant:2").manifest()).isEqualTo(original);
        assertThat(storage.activations).isEqualTo(1);
    }

    @Test
    void revalidationReusesStoredCandidateIdentityWithoutDeliveryContext() {
        Storage storage = new Storage();
        SecurityManifest manifest = merchant("Read");
        String storedIdentity = "existing-publication-id";
        SecurityManifestCandidate candidate = new SecurityManifestCandidate(storedIdentity, manifest,
                SecurityManifestCandidateStatus.WAITING_DEPENDENCY, null, Instant.now(), null);
        storage.save(candidate);
        RegisterSecurityManifestService registration = new RegisterSecurityManifestService(
                storage, storage, storage, () -> {
                    throw new AssertionError("revalidation requested a delivery UUID");
                });
        RevalidateSecurityManifestService service = new RevalidateSecurityManifestService(storage, storage, registration);
        RevalidateSecurityManifestCommand command = new RevalidateSecurityManifestCommand("merchant", 2);

        RegisterSecurityManifestResult result = service.execute(command);

        assertThat(result.outcome()).isEqualTo(SecurityManifestCandidateStatus.APPLIED.name());
        assertThat(storage.receipts).containsOnlyKeys(storedIdentity);
        assertThat(storage.candidates.get("merchant:2").eventId()).isEqualTo(storedIdentity);
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
        public boolean ensureInitialized(boolean creationAllowed) { return true; }

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
