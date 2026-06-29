package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.event.UserRegisteredEvent;
import com.identity.domain.event.UserStatusChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {
    @Test
    void createLocal_withValidIdentity_shouldCreateActiveUser() {
        User user = User.createLocal(
                new CommonId("u1"), new Email("USER@Example.com"), new HashedPassword("hash"));

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("user@example.com", user.getEmail().value());
        assertInstanceOf(UserRegisteredEvent.class, user.getEvents().getFirst());
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

    private User hydratedUser(UserStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new User(
                new CommonId("u1"),
                new Email("user@example.com"),
                new HashedPassword("hash"),
                status,
                now,
                now
        );
    }
}
