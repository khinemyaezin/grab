package com.merchant.application.service;

import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.RecordMemberProvisioningResultCommand;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.enums.AccessProvisioningStatus;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecordMemberProvisioningResultServiceTest {
    private MerchantMemberRepository repository;
    private RecordMemberProvisioningResultService service;

    private final CommonId merchantId = new CommonId("mer-1");
    private final CommonId userId = new CommonId("usr-1");
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        repository = mock(MerchantMemberRepository.class);
        service = new RecordMemberProvisioningResultService(repository);
    }

    @Test
    void execute_whenSuccess_shouldUpdateAndSaveMember() {
        MerchantMember member = MerchantMember.createAdmin(new CommonId("mem-1"), merchantId, userId, now);
        when(repository.findByMerchantIdAndUserId(merchantId, userId)).thenReturn(Optional.of(member));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordMemberProvisioningResultCommand command = new RecordMemberProvisioningResultCommand(
                merchantId, userId, AccessProvisioningStatus.ACTIVE, null, 0, now.plusSeconds(30)
        );

        var result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(member.getAccessProvisioningStatus()).isEqualTo(AccessProvisioningStatus.ACTIVE);
        verify(repository).save(member);
    }

    @Test
    void execute_whenFailed_shouldRecordError() {
        MerchantMember member = MerchantMember.createAdmin(new CommonId("mem-1"), merchantId, userId, now);
        when(repository.findByMerchantIdAndUserId(merchantId, userId)).thenReturn(Optional.of(member));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordMemberProvisioningResultCommand command = new RecordMemberProvisioningResultCommand(
                merchantId, userId, AccessProvisioningStatus.FAILED, "DEPENDENCY_TIMEOUT", 0, now.plusSeconds(30)
        );

        var result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(member.getAccessProvisioningStatus()).isEqualTo(AccessProvisioningStatus.FAILED);
        assertThat(member.getAccessProvisioningError()).isEqualTo("DEPENDENCY_TIMEOUT");
        verify(repository).save(member);
    }
}
