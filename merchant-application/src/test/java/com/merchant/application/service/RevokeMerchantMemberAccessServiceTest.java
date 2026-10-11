package com.merchant.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.RevokeMerchantMemberAccessCommand;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort.RevokeAccessRequest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class RevokeMerchantMemberAccessServiceTest {

    private IdentityAccessManagementPort identityAccessManagementPort;
    private RevokeMerchantMemberAccessService service;

    @BeforeEach
    void setUp() {
        identityAccessManagementPort = Mockito.mock(IdentityAccessManagementPort.class);
        service = new RevokeMerchantMemberAccessService(identityAccessManagementPort);
    }

    @Test
    void execute_whenInvoked_revokesAccessWithMappedRoleCode() {
        Id merchantId = new CommonId("mer-1");
        Id memberId = new CommonId("mem-1");
        Id userId = new CommonId("usr-1");
        String role = "OPERATOR";
        RevokeMerchantMemberAccessCommand command = new RevokeMerchantMemberAccessCommand(
                merchantId,
                memberId,
                userId,
                role
        );

        service.execute(command);

        ArgumentCaptor<RevokeAccessRequest> captor = ArgumentCaptor.forClass(RevokeAccessRequest.class);
        verify(identityAccessManagementPort).revokeAccess(captor.capture());
        RevokeAccessRequest captured = captor.getValue();
        assertThat(captured.userId()).isEqualTo("usr-1");
        assertThat(captured.roleCode()).isEqualTo("OPERATOR");
        assertThat(captured.scopeKey()).isEqualTo(MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY);
        assertThat(captured.scopeId()).isEqualTo("mer-1");
    }
}
