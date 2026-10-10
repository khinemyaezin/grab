package com.identity.domain.policy;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RolePermissionReference;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.security.CatalogAuthority;
import com.identity.domain.security.CatalogModule;
import com.identity.domain.security.CatalogScope;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProtectedRoleReconciliationPolicyTest {

    @Test
    void evaluate_whenAllDependenciesMet_shouldReturnReconciled() {
        var scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        var auth1 = new CatalogAuthority("CATALOG_READ", "catalog", Lifecycle.ACTIVE, true, 1);
        var auth2 = new CatalogAuthority("MERCHANT_PROFILE_READ", "merchant", Lifecycle.ACTIVE, true, 1);
        var modCatalog = new CatalogModule("catalog", 1, "digest-cat");
        var modMerchant = new CatalogModule("merchant", 1, "digest-mer");

        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(auth1, auth2), List.of(modCatalog, modMerchant)
        );

        AuthorityRepository authorityRepository = mock(AuthorityRepository.class);
        Authority domainAuth1 = Authority.create(new CommonId("a-1"), "CATALOG_READ", "catalog", "Catalog Read", "Read");
        Authority domainAuth2 = Authority.create(new CommonId("a-2"), "MERCHANT_PROFILE_READ", "merchant", "Merchant Profile Read", "Read");
        when(authorityRepository.findActiveByCodes(Set.of("CATALOG_READ", "MERCHANT_PROFILE_READ")))
                .thenReturn(Set.of(domainAuth1, domainAuth2));

        RoleDeclaration declaration = new RoleDeclaration(
                "merchant",
                "MERCHANT_ADMIN",
                "merchant.account",
                1,
                Map.of("catalog", 1, "merchant", 1),
                List.of(
                        new RolePermissionReference("catalog", "CATALOG_READ"),
                        new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ")
                )
        );

        var outcome = ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, authorityRepository);

        assertThat(outcome.isReconciled()).isTrue();
        assertThat(outcome.authorities()).containsExactlyInAnyOrder(domainAuth1, domainAuth2);
    }

    @Test
    void evaluate_whenModuleManifestMissing_shouldReturnWaitingDependency() {
        var scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(), List.of()
        );
        AuthorityRepository authorityRepository = mock(AuthorityRepository.class);

        RoleDeclaration declaration = new RoleDeclaration(
                "merchant",
                "MERCHANT_ADMIN",
                "merchant.account",
                1,
                Map.of("catalog", 1),
                List.of(new RolePermissionReference("catalog", "CATALOG_READ"))
        );

        var outcome = ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, authorityRepository);

        assertThat(outcome.isReconciled()).isFalse();
        assertThat(outcome.status()).isEqualTo(ProtectedRoleReconciliationPolicy.ReconciliationStatus.WAITING_DEPENDENCY);
        assertThat(outcome.reason()).contains("Waiting for module manifest: catalog");
    }

    @Test
    void evaluate_whenOwnerMismatch_shouldThrowException() {
        var scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        var auth1 = new CatalogAuthority("CATALOG_READ", "catalog", Lifecycle.ACTIVE, true, 1);
        var modCatalog = new CatalogModule("catalog", 1, "digest-cat");
        var modInventory = new CatalogModule("inventory", 1, "digest-inv");

        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(auth1), List.of(modCatalog, modInventory)
        );
        AuthorityRepository authorityRepository = mock(AuthorityRepository.class);

        RoleDeclaration declaration = new RoleDeclaration(
                "merchant",
                "MERCHANT_ADMIN",
                "merchant.account",
                1,
                Map.of("inventory", 1),
                List.of(new RolePermissionReference("inventory", "CATALOG_READ"))
        );

        assertThatThrownBy(() -> ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, authorityRepository))
                .isInstanceOf(IdentityDomainValidationException.class)
                .hasMessageContaining("Authority owner mismatch");
    }
}
