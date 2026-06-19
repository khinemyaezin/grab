package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void createLocal_withCustomerRole_shouldCreateActiveUser() {
        User user = User.createLocal(new CommonId("u1"), new Email("USER@Example.com"), new HashedPassword("hash"), "CUSTOMER");
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("user@example.com", user.getEmail().value());
    }

    @Test
    void createLocal_withSellerRole_shouldCreatePendingUser() {
        User user = User.createLocal(new CommonId("u1"), new Email("seller@example.com"), new HashedPassword("hash"), "SELLER");
        assertEquals(UserStatus.PENDING_APPROVAL, user.getStatus());
    }

    @Test
    void createLocal_withAdminRole_shouldRejectSelfRegistration() {
        assertThrows(IllegalArgumentException.class, () -> User.createLocal(new CommonId("u1"), new Email("admin@example.com"), new HashedPassword("hash"), "ADMIN"));
    }
}
