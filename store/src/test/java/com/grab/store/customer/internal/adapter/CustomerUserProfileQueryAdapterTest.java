package com.grab.store.customer.internal.adapter;

import com.customer.application.port.outbound.UserProfileQueryPort;
import com.grab.framework.id.Id;
import com.grab.store.identity.port.UserProfileQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUserProfileQueryAdapterTest {

    @Mock
    private UserProfileQuery identityUserProfileQuery;

    private UserProfileQueryPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerUserProfileQueryPortAdapter(identityUserProfileQuery);
    }

    @Test
    void shouldDelegateAndMapUserProfile() {
        Id userId = () -> "user-123";

        UserProfileQuery.UserProfileResponse identityResponse =
                new UserProfileQuery.UserProfileResponse(
                        "user-123", "test@example.com", "ACTIVE", "2023-01-01T00:00:00Z",
                        List.of("CUSTOMER_APP")
                );

        when(identityUserProfileQuery.getUserProfile(userId)).thenReturn(identityResponse);

        UserProfileQueryPort.UserProfileResponse response = adapter.getUserProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("user-123");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.createdAt()).isEqualTo("2023-01-01T00:00:00Z");
        assertThat(response.platformCodes()).containsExactly("CUSTOMER_APP");

        verify(identityUserProfileQuery).getUserProfile(userId);
    }

    @Test
    void shouldReturnNull_whenIdentityReturnsNull() {
        Id userId = () -> "user-123";
        when(identityUserProfileQuery.getUserProfile(userId)).thenReturn(null);

        UserProfileQueryPort.UserProfileResponse response = adapter.getUserProfile(userId);

        assertThat(response).isNull();
    }
}
