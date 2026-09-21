package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.store.identity.internal.api.adapter.mapper.UserProfileQueryMapper;
import com.grab.store.identity.port.UserProfileQuery;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.port.inbound.GetUserProfileUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileQueryAdapterTest {

    @Mock
    private GetUserProfileUseCase getUserProfileUseCase;

    private UserProfileQueryMapper mapper;
    private UserProfileQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UserProfileQueryMapper.class);
        adapter = new UserProfileQueryAdapter(getUserProfileUseCase, mapper);
    }

    @Test
    void getUserProfile_validUserId_returnsUserProfileResponse() {
        Id userId = () -> "user-123";

        GetUserProfileResult.AccessContextInfo context1 = new GetUserProfileResult.AccessContextInfo(
                "assign-1", "PLATFORM_1", "ROLE_1", "GLOBAL", "ALL", "ACTIVE"
        );
        GetUserProfileResult.AccessContextInfo context2 = new GetUserProfileResult.AccessContextInfo(
                "assign-2", "PLATFORM_2", "ROLE_2", "MERCHANT", "M-123", "ACTIVE"
        );
        GetUserProfileResult.AccessContextInfo duplicateContext = new GetUserProfileResult.AccessContextInfo(
                "assign-3", "PLATFORM_1", "ROLE_3", "GLOBAL", "ALL", "ACTIVE"
        );

        GetUserProfileResult result = new GetUserProfileResult(
                "user-123",
                "user@example.com",
                "ACTIVE",
                "2023-01-01T00:00:00Z",
                List.of(context1, context2, duplicateContext)
        );

        when(getUserProfileUseCase.execute(any(GetUserProfileQuery.class))).thenReturn(result);

        UserProfileQuery.UserProfileResponse response = adapter.getUserProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("user-123");
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.createdAt()).isEqualTo("2023-01-01T00:00:00Z");
        assertThat(response.platformCodes()).containsExactly("PLATFORM_1", "PLATFORM_2");

        verify(getUserProfileUseCase).execute(any(GetUserProfileQuery.class));
    }
}
