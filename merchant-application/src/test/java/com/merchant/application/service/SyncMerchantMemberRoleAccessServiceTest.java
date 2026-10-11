package com.merchant.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.SyncMerchantMemberRoleAccessCommand;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort.ReplaceAccessRequest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class SyncMerchantMemberRoleAccessServiceTest {

    private IdentityAccessManagementPort identityAccessManagementPort;
    private SyncMerchantMemberRoleAccessService service;

    @BeforeEach
    void setUp() {
        identityAccessManagementPort = Mockito.mock(IdentityAccessManagementPort.class);
        service = new SyncMerchantMemberRoleAccessService(identityAccessManagementPort);
    }

    @Test
    void execute_whenInvoked_replacesAccessWithMappedRoleCodes() {
        Id merchantId = new CommonId("mer-1");
        Id memberId = new CommonId("mem-1");
        Id userId = new CommonId("usr-1");
        String previousRole = "OPERATOR";
        String newRole = "ANALYST";
        Set<String> authorities = Set.of("PRODUCT_READ");
        SyncMerchantMemberRoleAccessCommand command = new SyncMerchantMemberRoleAccessCommand(
                merchantId,
                memberId,
                userId,
                previousRole,
                newRole,
                authorities
        );

        service.execute(command);

        ArgumentCaptor<ReplaceAccessRequest> captor = ArgumentCaptor.forClass(ReplaceAccessRequest.class);
        verify(identityAccessManagementPort).replaceAccess(captor.capture());
        ReplaceAccessRequest captured = captor.getValue();
        assertThat(captured.userId()).isEqualTo("usr-1");
        assertThat(captured.previousRoleCode()).isEqualTo("OPERATOR");
        assertThat(captured.roleCode()).isEqualTo("ANALYST");
        assertThat(captured.scopeKey()).isEqualTo(MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY);
        assertThat(captured.scopeId()).isEqualTo("mer-1");
        assertThat(captured.authorityCodes()).containsExactly("PRODUCT_READ");
    }
}
