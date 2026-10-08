package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.event.RoleAuthorityChangedEvent;
import com.identity.domain.event.RoleCreatedEvent;
import com.identity.domain.event.RoleStatusChangedEvent;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleTest {

    @Test
    void create_withValidDetails_shouldCreateActiveRoleAndEmitEvent() {
        Role role = Role.createCustom(
                new CommonId("r1"),
                " seller ",
                " Seller ",
                "Marketplace seller",
                Set.of(authority("CATALOG_READ"))
        );

        assertEquals("SELLER", role.getCode());
        assertEquals("Seller", role.getName());
        RoleCreatedEvent event = assertInstanceOf(RoleCreatedEvent.class, role.getEvents().getFirst());
        assertEquals(Set.of("CATALOG_READ"), event.authorityCodes());
    }

    @Test
    void create_withInvalidCode_shouldRejectRole() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> Role.createCustom(
                        new CommonId("r1"),
                        "not valid",
                        "Seller",
                        null,
                        Set.of(authority("CATALOG_READ"))
                )
        );

        assertInstanceOf(IdentityDomainError.InvalidRoleCode.class, exception.getMessageSource());
    }

    @Test
    void deactivate_withActiveRole_shouldDeactivateAndEmitEvent() {
        Role role = hydratedRole(true);

        role.deactivate();

        assertFalse(role.isActive());
        RoleStatusChangedEvent event = assertInstanceOf(RoleStatusChangedEvent.class, role.getEvents().getFirst());
        assertFalse(event.active());
    }

    @Test
    void assignAuthority_withNewAuthority_shouldEmitEvent() {
        Role role = hydratedRole(true);

        role.assignAuthority(authority("CATALOG_READ"));

        assertEquals(Set.of("CATALOG_READ"), role.getAuthorities().stream()
                .map(Authority::getCode)
                .collect(java.util.stream.Collectors.toSet()));
        RoleAuthorityChangedEvent event = assertInstanceOf(RoleAuthorityChangedEvent.class, role.getEvents().getFirst());
        assertEquals("CATALOG_READ", event.authorityCode());
        assertEquals(true, event.assigned());
    }

    @Test
    void assignAuthority_withExistingAuthority_shouldNotEmitEvent() {
        Role role = new Role(new CommonId("r1"), "SELLER", "Seller", null, true, Set.of(authority("CATALOG_READ")));

        role.assignAuthority(authority("CATALOG_READ"));

        assertEquals(0, role.getEvents().size());
    }

    @Test
    void assignAuthority_withSystemRole_shouldRejectRuntimeMutation() {
        Role role = Role.rehydrate(
                new CommonId("r1"),
                "MERCHANT_OWNER",
                "Merchant Owner",
                null,
                RoleKind.SYSTEM,
                true,
                true,
                Set.of(authority("MERCHANT_PROFILE_READ"))
        );

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> role.assignAuthority(authority("MERCHANT_PROFILE_WRITE"))
        );

        assertInstanceOf(
                IdentityDomainError.SystemRoleModificationForbidden.class,
                exception.getMessageSource()
        );
    }

    @Test
    void revokeAuthority_withLastCustomAuthority_shouldRejectEmptyRole() {
        Role role = hydratedRole(true);
        role.assignAuthority(authority("CATALOG_READ"));
        role.pullEvents();

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> role.revokeAuthority(authority("CATALOG_READ"))
        );

        assertInstanceOf(IdentityDomainError.RoleAuthoritiesRequired.class, exception.getMessageSource());
    }

    private Role hydratedRole(boolean active) {
        return new Role(new CommonId("r1"), "SELLER", "Seller", null, active, Set.of());
    }

    private Authority authority(String code) {
        return Authority.rehydrate(new CommonId("authority-" + code), code, "identity", code, null, true);
    }
}
