package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.*;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.identity.domain.security.*;
import com.identity.domain.valueobject.AccessScope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecurityCatalogTest {
    @Test
    void consider_sameRevisionNewReceipt_doesNotActivateAgain() {
        var catalog = empty();
        var manifest = merchant(2, Lifecycle.ACTIVE);
        var first = consider(catalog, manifest, Optional.empty(), 0);
        var replay = consider(catalog, manifest, Optional.empty(), 0);
        assertTrue(first.newlyActivated());
        assertFalse(replay.newlyActivated());
        assertEquals(SecurityManifestCandidateStatus.APPLIED, replay.status());
        assertEquals(1, catalog.revision());
        assertEquals(1, catalog.pullEvents().size());
    }

    @Test
    void consider_lowerRevision_doesNotDowngrade() {
        var catalog = empty();
        consider(catalog, merchant(3, Lifecycle.ACTIVE), Optional.empty(), 0);
        var decision = consider(catalog, merchant(2, Lifecycle.ACTIVE), Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.SUPERSEDED, decision.status());
        assertEquals(1, catalog.revision());
    }

    @Test
    void consider_conflictingAppliedRevision_isQuarantinedWithoutChangingCatalog() {
        var catalog = empty();
        consider(catalog, merchant(2, Lifecycle.ACTIVE), Optional.empty(), 0);
        var decision = consider(catalog, merchant(2, Lifecycle.RETIRED), Optional.empty(), 0);
        assertTrue(decision.conflict());
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, decision.status());
        assertTrue(catalog.hierarchy().isEffective("merchant.account"));
        assertEquals(1, catalog.revision());
    }

    @Test
    void consider_missingDependency_waitsButPermanentDefectIsQuarantinedFirst() {
        var catalog = empty();
        var manifest = new SecurityManifest("inventory", 2,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")), List.of(),
                List.of(new SecurityDependency("merchant.account", 2)));
        var waiting = consider(catalog, manifest, Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.WAITING_DEPENDENCY, waiting.status());
        var invalid = new SecurityManifest("inventory", 3,
                List.of(new ScopeDeclaration("inventory.location", "inventory.location")), List.of(),
                List.of(new SecurityDependency("merchant.account", 2)));
        var rejected = consider(catalog, invalid, Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, rejected.status());
        assertEquals(0, catalog.revision());
        assertTrue(catalog.pullEvents().isEmpty());
    }

    @Test
    void consider_parentArrives_waitingCandidateCanActivate() {
        var catalog = empty();
        var manifest = new SecurityManifest("inventory", 2,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")), List.of(),
                List.of(new SecurityDependency("merchant.account", 2)));
        var waiting = consider(catalog, manifest, Optional.empty(), 0);
        var candidate = new SecurityManifestCandidate("event", manifest, waiting.status(), waiting.errorCode(), Instant.now(), null);
        consider(catalog, merchant(2, Lifecycle.ACTIVE), Optional.empty(), 0);
        var applied = consider(catalog, manifest, Optional.of(candidate), 2);
        assertTrue(applied.newlyActivated());
        assertNull(applied.errorCode());
        assertTrue(catalog.hierarchy().isEffective("inventory.location"));
    }

    @Test
    void consider_foreignOwnerOrOmission_preservesActiveCatalog() {
        var catalog = empty();
        consider(catalog, merchant(2, Lifecycle.ACTIVE), Optional.empty(), 0);
        var theft = new SecurityManifest("inventory", 3,
                List.of(new ScopeDeclaration("merchant.account", null)), List.of());
        var conflict = consider(catalog, theft, Optional.empty(), 0);
        var omission = new SecurityManifest("merchant", 3, List.of(), List.of());
        var missing = consider(catalog, omission, Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, conflict.status());
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, missing.status());
        assertEquals(1, catalog.revision());
    }

    @Test
    void consider_cycleOrReparent_rejectsWholeCandidate() {
        var catalog = empty();
        var cycle = new SecurityManifest("merchant", 2, List.of(
                new ScopeDeclaration("merchant.account", "merchant.store"),
                new ScopeDeclaration("merchant.store", "merchant.account")), List.of());
        var decision = consider(catalog, cycle, Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, decision.status());
        consider(catalog, merchant(2, Lifecycle.ACTIVE), Optional.empty(), 0);
        var reparent = new SecurityManifest("merchant", 3,
                List.of(new ScopeDeclaration("merchant.account", "global")),
                List.of(new AuthorityDefinition("MERCHANT_READ", "Read", null)));
        var rejected = consider(catalog, reparent, Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, rejected.status());
    }

    @Test
    void consider_parentRetirement_disablesDescendantsAndCannotReactivate() {
        var catalog = empty();
        consider(catalog, merchant(2, Lifecycle.ACTIVE), Optional.empty(), 0);
        var inventory = new SecurityManifest("inventory", 2,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")), List.of());
        consider(catalog, inventory, Optional.empty(), 0);
        consider(catalog, merchant(3, Lifecycle.RETIRED), Optional.empty(), 0);
        var hierarchy = catalog.hierarchy();
        assertFalse(hierarchy.isEffective("inventory.location"));
        var target = AccessScope.from("inventory.location", "location");
        assertFalse(AccessScope.global().encompasses(target, hierarchy));
        assertFalse(target.encompasses(target, hierarchy));
        var reintroduced = consider(catalog, merchant(4, Lifecycle.ACTIVE), Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, reintroduced.status());
    }

    @Test
    void consider_republication_preservesLocalDisablement() {
        var scopes = List.of(new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, false, 2));
        var catalog = SecurityCatalog.rehydrate(new CommonId("catalog"), 1, scopes, List.of(), List.of());
        var manifest = new SecurityManifest("merchant", 2,
                List.of(new ScopeDeclaration("merchant.account", null)), List.of());
        var decision = consider(catalog, manifest, Optional.empty(), 0);
        assertTrue(decision.newlyActivated());
        assertFalse(catalog.hierarchy().isEffective("merchant.account"));
        assertFalse(catalog.hierarchy().isEffective("unknown.scope"));
    }

    @Test
    void hierarchy_unestablishedMetadataAndAuthorityDisablement_failClosed() {
        var scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 0);
        var catalog = SecurityCatalog.rehydrate(new CommonId("catalog"), 0, List.of(scope), List.of(), List.of());
        assertFalse(catalog.hierarchy().isEffective("merchant.account"));
        assertFalse(catalog.hierarchy().isEffective(null));
        var authority = new CatalogAuthority("MERCHANT_READ", "merchant", Lifecycle.ACTIVE, false, 2);
        assertFalse(authority.isEffective());
    }

    @Test
    void consider_omittedAuthorityAndRetirementTombstone_areRejected() {
        var catalog = empty();
        var active = merchant(2, Lifecycle.ACTIVE);
        consider(catalog, active, Optional.empty(), 0);
        var omitted = new SecurityManifest("merchant", 3, active.scopes(), List.of());
        var decision = consider(catalog, omitted, Optional.empty(), 0);
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, decision.status());
        assertEquals("idt.domain.security_manifest.omitted_authority", decision.errorCode());
        consider(catalog, merchant(3, Lifecycle.RETIRED), Optional.empty(), 0);
        var tombstoneMissing = new SecurityManifest("merchant", 4, List.of(), List.of());
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, consider(catalog, tombstoneMissing, Optional.empty(), 0).status());
    }

    @Test
    void consider_permanentDependencyDefect_precedesMissingDependencyWait() {
        var catalog = empty();
        var malformed = new SecurityManifest("inventory", 9, List.of(), List.of(),
                List.of(new SecurityDependency("missing.scope", 2), new SecurityDependency("invalid", 1)));
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, consider(catalog, malformed, Optional.empty(), 0).status());
        var self = new SecurityManifest("inventory", 3,
                List.of(new ScopeDeclaration("inventory.location", null)), List.of(),
                List.of(new SecurityDependency("inventory.location", 3)));
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, consider(catalog, self, Optional.empty(), 0).status());
        consider(catalog, merchant(2, Lifecycle.RETIRED), Optional.empty(), 0);
        var retired = new SecurityManifest("inventory", 4, List.of(), List.of(),
                List.of(new SecurityDependency("merchant.account", 2)));
        assertEquals(SecurityManifestCandidateStatus.QUARANTINED, consider(catalog, retired, Optional.empty(), 0).status());
    }

    private SecurityCatalog empty() {
        var id = new CommonId("catalog");
        return SecurityCatalog.rehydrate(id, 0, List.of(), List.of(), List.of());
    }

    private SecurityManifest merchant(int revision, Lifecycle lifecycle) {
        var scope = new ScopeDeclaration("merchant.account", null, lifecycle);
        var authority = new AuthorityDefinition("MERCHANT_READ", "Read", null, "merchant", lifecycle);
        return new SecurityManifest("merchant", revision, List.of(scope), List.of(authority));
    }

    private SecurityManifestDecision consider(SecurityCatalog catalog, SecurityManifest manifest,
            Optional<SecurityManifestCandidate> canonical, int highest) {
        String digest = manifest.contentDigest();
        return catalog.consider(manifest, digest, "event", Optional.empty(), canonical, highest);
    }
}
