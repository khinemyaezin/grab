package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.event.UserRegisteredEvent;
import com.identity.domain.event.UserRoleChangedEvent;
import com.identity.domain.event.UserStatusChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {
    @Test
    void createLocal_withCustomerRole_shouldCreateActiveUser() {
        User user = User.createLocal(new CommonId("u1"), new Email("USER@Example.com"), new HashedPassword("hash"), "CUSTOMER");

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("user@example.com", user.getEmail().value());
        assertInstanceOf(UserRegisteredEvent.class, user.getEvents().getFirst());
    }

    @Test
    void createLocal_withSellerRole_shouldCreatePendingUser() {
        User user = User.createLocal(
                new CommonId("u1"),
                new Email("seller@example.com"),
                new HashedPassword("hash"),
                " seller "
        );

        assertEquals(UserStatus.PENDING_APPROVAL, user.getStatus());
        assertEquals(Set.of("SELLER"), user.getRoleCodes());
    }

    @Test
    void createLocal_withAdminRole_shouldRejectSelfRegistration() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> User.createLocal(
                        new CommonId("u1"),
                        new Email("admin@example.com"),
                        new HashedPassword("hash"),
                        "ADMIN"
                )
        );

        assertInstanceOf(IdentityDomainError.InvalidSelfRegistrationRole.class, exception.getMessageSource());
    }

    @Test
    void activate_withPendingUser_shouldActivateAndEmitEvent() {
        User user = hydratedUser(UserStatus.PENDING_APPROVAL);

        user.activate();

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        UserStatusChangedEvent event = assertInstanceOf(UserStatusChangedEvent.class, user.getEvents().getFirst());
        assertEquals(UserStatus.PENDING_APPROVAL, event.previousStatus());
        assertEquals(UserStatus.ACTIVE, event.currentStatus());
    }

    @Test
    void activate_withActiveUser_shouldRejectTransition() {
        User user = hydratedUser(UserStatus.ACTIVE);

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                user::activate
        );

        assertInstanceOf(IdentityDomainError.InvalidUserStatusTransition.class, exception.getMessageSource());
    }

    @Test
    void assignRole_withNewRole_shouldNormalizeAndEmitEvent() {
        User user = hydratedUser(UserStatus.ACTIVE);

        user.assignRole(" seller ");

        assertEquals(Set.of("CUSTOMER", "SELLER"), user.getRoleCodes());
        UserRoleChangedEvent event = assertInstanceOf(UserRoleChangedEvent.class, user.getEvents().getFirst());
        assertEquals("SELLER", event.roleCode());
        assertEquals(true, event.assigned());
    }

    @Test
    void assignRole_withExistingRole_shouldNotEmitEvent() {
        User user = hydratedUser(UserStatus.ACTIVE);

        user.assignRole("customer");

        assertEquals(0, user.getEvents().size());
    }

    private User hydratedUser(UserStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new User(
                new CommonId("u1"),
                new Email("user@example.com"),
                new HashedPassword("hash"),
                Set.of("CUSTOMER"),
                status,
                now,
                now
        );
    }
}
