package com.identity.domain.policy;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RolePermissionReference;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.security.CatalogAuthority;
import com.identity.domain.security.CatalogModule;
import com.identity.domain.security.CatalogScope;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProtectedRoleReconciliationPolicyTest {

    @Test
    void evaluateWhenAllDependenciesAreMetReturnsReconciledAuthorities() {
        CatalogScope scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        CatalogAuthority catalogAuthority =
                new CatalogAuthority("CATALOG_READ", "catalog", Lifecycle.ACTIVE, true, 1);
        CatalogAuthority merchantAuthority =
                new CatalogAuthority("MERCHANT_PROFILE_READ", "merchant", Lifecycle.ACTIVE, true, 1);
        CatalogModule catalogModule = new CatalogModule("catalog", 1, "digest-cat");
        CatalogModule merchantModule = new CatalogModule("merchant", 1, "digest-mer");
        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(catalogAuthority, merchantAuthority),
                List.of(catalogModule, merchantModule));
        Authority domainCatalogAuthority =
                Authority.create(new CommonId("a-1"), "CATALOG_READ", "catalog", "Catalog Read", "Read");
        Authority domainMerchantAuthority = Authority.create(
                new CommonId("a-2"), "MERCHANT_PROFILE_READ", "merchant", "Merchant Profile Read", "Read");
        RoleDeclaration declaration = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
                Map.of("catalog", 1, "merchant", 1),
                List.of(new RolePermissionReference("catalog", "CATALOG_READ"),
                        new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ")));

        ProtectedRoleReconciliationPolicy.ReconciliationOutcome outcome =
                ProtectedRoleReconciliationPolicy.evaluate(
                        declaration, catalog, Set.of(domainCatalogAuthority, domainMerchantAuthority));

        assertThat(outcome.isReconciled()).isTrue();
        assertThat(outcome.authorities()).containsExactlyInAnyOrder(domainCatalogAuthority, domainMerchantAuthority);
    }

    @Test
    void evaluateWhenModuleManifestIsMissingReturnsWaitingDependency() {
        CatalogScope scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(), List.of());
        RoleDeclaration declaration = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
                Map.of("catalog", 1), List.of(new RolePermissionReference("catalog", "CATALOG_READ")));

        ProtectedRoleReconciliationPolicy.ReconciliationOutcome outcome =
                ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, Set.of());

        assertThat(outcome.status())
                .isEqualTo(ProtectedRoleReconciliationPolicy.ReconciliationStatus.WAITING_DEPENDENCY);
        assertThat(outcome.reason()).contains("Waiting for module manifest: catalog");
    }

    @Test
    void evaluateWhenPermissionOwnerDoesNotMatchCatalogQuarantinesDeclaration() {
        CatalogScope scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        CatalogAuthority catalogAuthority =
                new CatalogAuthority("CATALOG_READ", "catalog", Lifecycle.ACTIVE, true, 1);
        CatalogModule inventoryModule = new CatalogModule("inventory", 1, "digest-inv");
        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(catalogAuthority), List.of(inventoryModule));
        RoleDeclaration declaration = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
                Map.of("inventory", 1), List.of(new RolePermissionReference("inventory", "CATALOG_READ")));

        ProtectedRoleReconciliationPolicy.ReconciliationOutcome outcome =
                ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, Set.of());

        assertThat(outcome.status())
                .isEqualTo(ProtectedRoleReconciliationPolicy.ReconciliationStatus.INVALID_DECLARATION);
        assertThat(outcome.reason()).contains("Authority owner mismatch");
    }

    @Test
    void evaluateWhenDeclarationHasNoPermissionsQuarantinesDeclaration() {
        SecurityCatalog catalog = SecurityCatalog.rehydrate(new CommonId("cat-1"), 1L, List.of(), List.of(), List.of());
        RoleDeclaration declaration = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1, Map.of(), List.of());

        ProtectedRoleReconciliationPolicy.ReconciliationOutcome outcome =
                ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, Set.of());

        assertThat(outcome.status())
                .isEqualTo(ProtectedRoleReconciliationPolicy.ReconciliationStatus.INVALID_DECLARATION);
        assertThat(outcome.reason()).contains("at least one permission");
    }
}
