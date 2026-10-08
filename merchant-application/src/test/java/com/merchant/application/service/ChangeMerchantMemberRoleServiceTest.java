package com.merchant.application.service;

import com.grab.framework.id.impl.CommonId;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.enums.MemberStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.policy.MerchantOwnershipPolicy;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.valueobject.MerchantRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChangeMerchantMemberRoleServiceTest {
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");
    private final CommonId merchantId = new CommonId("mer-1");
    private final CommonId applicantId = new CommonId("usr-applicant");
    private final CommonId targetMemberId = new CommonId("mem-target");
    private final CommonId targetUserId = new CommonId("usr-target");
    private final CommonId staffUserId = new CommonId("usr-staff");

    private MerchantAccountRepository merchants;
    private MerchantMemberRepository members;
    private MerchantOwnershipPolicy ownershipPolicy;
    private ChangeMerchantMemberRoleService service;

    @BeforeEach
    void setUp() {
        merchants = Mockito.mock(MerchantAccountRepository.class);
        members = Mockito.mock(MerchantMemberRepository.class);
        ownershipPolicy = new MerchantOwnershipPolicy();
        service = new ChangeMerchantMemberRoleService(merchants, members, ownershipPolicy);

        when(members.save(any(MerchantMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void changeRole_byApplicant_shouldSucceed() {
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId, applicantId, MerchantType.FIRST_PARTY_RETAILER, "Test Merchant", now
        );
        when(merchants.findById(merchantId)).thenReturn(Optional.of(merchant));

        MerchantRole operatorRole = MerchantRole.of("OPERATOR");
        MerchantMember target = new MerchantMember(
                targetMemberId, merchantId, targetUserId,
                operatorRole, MemberStatus.ACTIVE, applicantId, null, now, now, now, 0
        );
        when(members.findById(targetMemberId)).thenReturn(Optional.of(target));

        MerchantRole supervisorRole = MerchantRole.of("SUPERVISOR");
        ChangeMerchantMemberRoleCommand command = new ChangeMerchantMemberRoleCommand(
                merchantId, targetMemberId, applicantId, supervisorRole
        );

        MerchantMemberResult result = service.execute(command);
        assertThat(result.role()).isEqualTo("SUPERVISOR");
    }

    @Test
    void changeRole_byStaff_shouldFailUnauthorized() {
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId, applicantId, MerchantType.FIRST_PARTY_RETAILER, "Test Merchant", now
        );
        when(merchants.findById(merchantId)).thenReturn(Optional.of(merchant));

        MerchantRole operatorRole = MerchantRole.of("OPERATOR");
        MerchantMember staffMember = new MerchantMember(
                new CommonId("mem-staff"), merchantId, staffUserId,
                operatorRole, MemberStatus.ACTIVE, applicantId, null, now, now, now, 0
        );
        when(members.findByMerchantIdAndUserId(merchantId, staffUserId)).thenReturn(Optional.of(staffMember));

        MerchantRole supervisorRole = MerchantRole.of("SUPERVISOR");
        ChangeMerchantMemberRoleCommand command = new ChangeMerchantMemberRoleCommand(
                merchantId, targetMemberId, staffUserId, supervisorRole
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(MerchantServiceException.class);
    }

    @Test
    void changeRole_whenDemotingSoleAdmin_shouldThrowDomainException() {
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId, applicantId, MerchantType.FIRST_PARTY_RETAILER, "Test Merchant", now
        );
        when(merchants.findById(merchantId)).thenReturn(Optional.of(merchant));

        MerchantMember soleAdmin = MerchantMember.createAdmin(targetMemberId, merchantId, targetUserId, now);
        when(members.findById(targetMemberId)).thenReturn(Optional.of(soleAdmin));
        when(members.countActiveAdmins(merchantId)).thenReturn(1L);

        MerchantRole supervisorRole = MerchantRole.of("SUPERVISOR");
        ChangeMerchantMemberRoleCommand command = new ChangeMerchantMemberRoleCommand(
                merchantId, targetMemberId, applicantId, supervisorRole
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(MerchantDomainException.class);
    }
}
