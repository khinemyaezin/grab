package com.merchant.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.enums.MemberStatus;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.valueobject.MerchantRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProvisionMerchantAdminServiceTest {
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");
    private final CommonId merchantId = new CommonId("mer-1");
    private final CommonId applicantId = new CommonId("usr-applicant");

    private MerchantMemberRepository members;
    private IdGenerator ids;
    private ProvisionMerchantAdminService service;

    @BeforeEach
    void setUp() {
        members = Mockito.mock(MerchantMemberRepository.class);
        ids = Mockito.mock(IdGenerator.class);
        service = new ProvisionMerchantAdminService(members, ids);

        when(ids.generateId()).thenReturn(new CommonId("mem-1"));
        when(members.save(any(MerchantMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_whenMemberDoesNotExist_shouldCreateInitialAdmin() {
        when(members.existsByMerchantIdAndUserId(merchantId, applicantId)).thenReturn(false);

        ProvisionMerchantAdminCommand command = new ProvisionMerchantAdminCommand(merchantId, applicantId, now);
        MerchantMemberResult result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo("MERCHANT_ADMIN");
        assertThat(result.status()).isEqualTo("ACTIVE");

        ArgumentCaptor<MerchantMember> captor = ArgumentCaptor.forClass(MerchantMember.class);
        verify(members).save(captor.capture());
        MerchantMember saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(MerchantRole.merchantAdmin());
        assertThat(saved.isAdmin()).isTrue();
        assertThat(saved.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void execute_whenMemberAlreadyExists_shouldReturnExistingWithoutSaving() {
        MerchantMember existing = MerchantMember.createAdmin(
                new CommonId("mem-existing"), merchantId, applicantId, now
        );
        when(members.existsByMerchantIdAndUserId(merchantId, applicantId)).thenReturn(true);
        when(members.findByMerchantIdAndUserId(merchantId, applicantId)).thenReturn(Optional.of(existing));

        ProvisionMerchantAdminCommand command = new ProvisionMerchantAdminCommand(merchantId, applicantId, now);
        MerchantMemberResult result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo("mem-existing");
        verify(members, never()).save(any());
    }
}
