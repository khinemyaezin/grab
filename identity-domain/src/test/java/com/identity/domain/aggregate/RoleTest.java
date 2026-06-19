package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.event.RoleAuthorityChangedEvent;
import com.identity.domain.event.RoleCreatedEvent;
import com.identity.domain.event.RoleStatusChangedEvent;
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
        Role role = Role.create(new CommonId("r1"), " seller ", " Seller ", "Marketplace seller");

        assertEquals("SELLER", role.getCode());
        assertEquals("Seller", role.getName());
        assertInstanceOf(RoleCreatedEvent.class, role.getEvents().getFirst());
    }

    @Test
    void create_withInvalidCode_shouldRejectRole() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> Role.create(new CommonId("r1"), "not valid", "Seller", null)
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
    void assignAuthority_withNewAuthority_shouldNormalizeAndEmitEvent() {
        Role role = hydratedRole(true);

        role.assignAuthority(" catalog_read ");

        assertEquals(Set.of("CATALOG_READ"), role.getAuthorityCodes());
        RoleAuthorityChangedEvent event = assertInstanceOf(RoleAuthorityChangedEvent.class, role.getEvents().getFirst());
        assertEquals("CATALOG_READ", event.authorityCode());
        assertEquals(true, event.assigned());
    }

    @Test
    void assignAuthority_withExistingAuthority_shouldNotEmitEvent() {
        Role role = new Role(new CommonId("r1"), "SELLER", "Seller", null, true, Set.of("CATALOG_READ"));

        role.assignAuthority("catalog_read");

        assertEquals(0, role.getEvents().size());
    }

    private Role hydratedRole(boolean active) {
        return new Role(new CommonId("r1"), "SELLER", "Seller", null, active, Set.of());
    }
}
