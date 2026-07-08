package com.grab.store.identity.internal.query.handler;

import com.grab.framework.id.Id;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.grab.store.identity.internal.query.GetUserProfileResult;
import com.identity.infrastructure.repository.jpa.UserQueryRepository;
import com.identity.infrastructure.view.UserAssignmentView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserProfileQueryHandlerTest {

    @Mock
    private UserQueryRepository repository;

    @InjectMocks
    private GetUserProfileQueryHandler handler;

    @Test
    void shouldReturnUser_withMultipleAssignments() {
        String userIdString = "user-123";
        Id userId = () -> userIdString;
        GetUserProfileQuery query = new GetUserProfileQuery(userId);

        UserAssignmentView view1 = new UserAssignmentView(
                userIdString,
                "user@example.com",
                "ACTIVE",
                "2023-01-01T00:00:00Z",
                "assign-1",
                "PLATFORM_1",
                "ROLE_1",
                "GLOBAL",
                "ALL",
                "ACTIVE"
        );

        UserAssignmentView view2 = new UserAssignmentView(
                userIdString,
                "user@example.com",
                "ACTIVE",
                "2023-01-01T00:00:00Z",
                "assign-2",
                "PLATFORM_2",
                "ROLE_2",
                "MERCHANT",
                "M-123",
                "ACTIVE"
        );

        when(repository.queryUserAndByUserId(userIdString)).thenReturn(Arrays.asList(view1, view2));
        GetUserProfileResult result = handler.handle(query);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userIdString);
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.createdAt()).isEqualTo("2023-01-01T00:00:00Z");
        
        assertThat(result.accessContexts()).hasSize(2);
        
        GetUserProfileResult.AccessContextInfo context1 = result.accessContexts().getFirst();
        assertThat(context1.assignmentId()).isEqualTo("assign-1");
        assertThat(context1.platformCode()).isEqualTo("PLATFORM_1");
        assertThat(context1.roleCode()).isEqualTo("ROLE_1");
        assertThat(context1.scopeKey()).isEqualTo("GLOBAL");
        assertThat(context1.scopeId()).isEqualTo("ALL");

        GetUserProfileResult.AccessContextInfo context2 = result.accessContexts().get(1);
        assertThat(context2.assignmentId()).isEqualTo("assign-2");
        assertThat(context2.platformCode()).isEqualTo("PLATFORM_2");
        assertThat(context2.roleCode()).isEqualTo("ROLE_2");
        assertThat(context2.scopeKey()).isEqualTo("MERCHANT");
        assertThat(context2.scopeId()).isEqualTo("M-123");
    }
}
