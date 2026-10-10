package com.identity.application.service;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RolePermissionReference;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.security.CatalogAuthority;
import com.identity.domain.security.CatalogModule;
import com.identity.domain.security.CatalogScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RegisterRoleDeclarationServiceTest {
    private SecurityCatalogRepository catalogs;
    private RoleRepository roles;
    private AuthorityRepository authorities;
    private RegisterRoleDeclarationService service;

    @BeforeEach
    void setUp() {
        catalogs = mock(SecurityCatalogRepository.class);
        roles = mock(RoleRepository.class);
        authorities = mock(AuthorityRepository.class);
        service = new RegisterRoleDeclarationService(catalogs, roles, authorities);
    }

    @Test
    void execute_whenDependenciesMet_shouldReconcileRoleAndMakeAssignable() {
        var scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        var auth = new CatalogAuthority("CATALOG_READ", "catalog", Lifecycle.ACTIVE, true, 1);
        var mod = new CatalogModule("catalog", 1, "digest");

        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(auth), List.of(mod)
        );
        when(catalogs.loadForUpdate()).thenReturn(catalog);

        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM, true, false, Set.of()
        );
        when(roles.findByCode("MERCHANT_ADMIN")).thenReturn(Optional.of(role));

        Authority domainAuth = Authority.create(new CommonId("a-1"), "CATALOG_READ", "catalog", "Catalog Read", "Read");
        when(authorities.findActiveByCodes(Set.of("CATALOG_READ"))).thenReturn(Set.of(domainAuth));

        RoleDeclaration declaration = new RoleDeclaration(
                "merchant",
                "MERCHANT_ADMIN",
                "merchant.account",
                1,
                Map.of("catalog", 1),
                List.of(new RolePermissionReference("catalog", "CATALOG_READ"))
        );

        var result = service.execute(new RegisterRoleDeclarationCommand(declaration, "event-1"));

        assertThat(result.outcome()).isEqualTo("RECONCILED");
        assertThat(role.isAssignable()).isTrue();
        assertThat(role.getAuthorities()).containsExactly(domainAuth);
        verify(roles).save(role);
    }

    @Test
    void execute_whenDependenciesMissing_shouldKeepRoleUnassignable() {
        var scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        SecurityCatalog catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(), List.of()
        );
        when(catalogs.loadForUpdate()).thenReturn(catalog);

        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM, true, false, Set.of()
        );
        when(roles.findByCode("MERCHANT_ADMIN")).thenReturn(Optional.of(role));

        RoleDeclaration declaration = new RoleDeclaration(
                "merchant",
                "MERCHANT_ADMIN",
                "merchant.account",
                1,
                Map.of("catalog", 1),
                List.of(new RolePermissionReference("catalog", "CATALOG_READ"))
        );

        var result = service.execute(new RegisterRoleDeclarationCommand(declaration, "event-1"));

        assertThat(result.outcome()).isEqualTo("WAITING_DEPENDENCY");
        assertThat(role.isAssignable()).isFalse();
        verify(roles).save(role);
    }
}
