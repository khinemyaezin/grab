package com.merchant.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.enums.MemberStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InviteMerchantMemberServiceTest {
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");
    private final CommonId merchantId = new CommonId("mer-1");
    private final CommonId applicantId = new CommonId("usr-applicant");
    private final CommonId adminMemberUserId = new CommonId("usr-admin-member");
    private final CommonId nonAdminUserId = new CommonId("usr-non-admin");
    private final CommonId targetUserId = new CommonId("usr-target");

    private MerchantAccountRepository merchants;
    private MerchantMemberRepository members;
    private IdGenerator ids;
    private InviteMerchantMemberService service;

    @BeforeEach
    void setUp() {
        merchants = Mockito.mock(MerchantAccountRepository.class);
        members = Mockito.mock(MerchantMemberRepository.class);
        ids = Mockito.mock(IdGenerator.class);
        service = new InviteMerchantMemberService(merchants, members, ids);

        when(ids.generateId()).thenReturn(new CommonId("new-member-id"));
        when(members.save(any(MerchantMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void invite_byApplicant_shouldSucceed() {
        MerchantAccount merchant = activeMerchant();
        when(merchants.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(members.existsByMerchantIdAndUserId(merchantId, targetUserId)).thenReturn(false);

        MerchantRole operatorRole = MerchantRole.of("OPERATOR");
        InviteMerchantMemberCommand command = new InviteMerchantMemberCommand(
                merchantId, applicantId, targetUserId, operatorRole, now.plusSeconds(86400)
        );

        MerchantMemberResult result = service.execute(command);

        assertThat(result.role()).isEqualTo("OPERATOR");
        assertThat(result.status()).isEqualTo("INVITED");
    }

    @Test
    void invite_byAdminMember_shouldSucceed() {
        MerchantAccount merchant = activeMerchant();
        when(merchants.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(members.existsByMerchantIdAndUserId(merchantId, targetUserId)).thenReturn(false);

        MerchantMember adminMember = new MerchantMember(
                new CommonId("mem-admin"), merchantId, adminMemberUserId,
                MerchantRole.merchantAdmin(), MemberStatus.ACTIVE, applicantId, null, now, now, now, 0
        );
        when(members.findByMerchantIdAndUserId(merchantId, adminMemberUserId)).thenReturn(Optional.of(adminMember));

        MerchantRole operatorRole = MerchantRole.of("OPERATOR");
        InviteMerchantMemberCommand command = new InviteMerchantMemberCommand(
                merchantId, adminMemberUserId, targetUserId, operatorRole, now.plusSeconds(86400)
        );

        MerchantMemberResult result = service.execute(command);

        assertThat(result.role()).isEqualTo("OPERATOR");
    }

    @Test
    void invite_byNonAdminMember_shouldFailUnauthorized() {
        MerchantAccount merchant = activeMerchant();
        when(merchants.findById(merchantId)).thenReturn(Optional.of(merchant));

        MerchantMember nonAdminMember = new MerchantMember(
                new CommonId("mem-non-admin"), merchantId, nonAdminUserId,
                MerchantRole.of("VIEWER"), MemberStatus.ACTIVE, applicantId, null, now, now, now, 0
        );
        when(members.findByMerchantIdAndUserId(merchantId, nonAdminUserId)).thenReturn(Optional.of(nonAdminMember));

        MerchantRole operatorRole = MerchantRole.of("OPERATOR");
        InviteMerchantMemberCommand command = new InviteMerchantMemberCommand(
                merchantId, nonAdminUserId, targetUserId, operatorRole, now.plusSeconds(86400)
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(MerchantServiceException.class);
    }

    @Test
    void invite_whenMerchantNotOperational_shouldFail() {
        MerchantAccount draftMerchant = MerchantAccount.startDraft(
                merchantId, applicantId, MerchantType.FIRST_PARTY_RETAILER, "Test Merchant", now
        );
        when(merchants.findById(merchantId)).thenReturn(Optional.of(draftMerchant));

        MerchantRole operatorRole = MerchantRole.of("OPERATOR");
        InviteMerchantMemberCommand command = new InviteMerchantMemberCommand(
                merchantId, applicantId, targetUserId, operatorRole, now.plusSeconds(86400)
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void invite_whenDuplicateMember_shouldFail() {
        MerchantAccount merchant = activeMerchant();
        when(merchants.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(members.existsByMerchantIdAndUserId(merchantId, targetUserId)).thenReturn(true);

        MerchantRole operatorRole = MerchantRole.of("OPERATOR");
        InviteMerchantMemberCommand command = new InviteMerchantMemberCommand(
                merchantId, applicantId, targetUserId, operatorRole, now.plusSeconds(86400)
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(MerchantDomainException.class);
    }

    private MerchantAccount activeMerchant() {
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId, applicantId, MerchantType.FIRST_PARTY_RETAILER, "Test Merchant", now
        );
        merchant.updateProfile(
                new MerchantName("Legal Name", "Test Merchant"),
                new BusinessRegistration("MM", "REG-001"),
                new ContactInformation("owner@example.com", "+959123456789"),
                new RegisteredAddress("1 Main Road", null, "Yangon", "Yangon", "11181", "MM"),
                now
        );
        merchant.submit(applicantId, now);
        merchant.approve(new CommonId("reviewer"), now);
        return merchant;
    }
}
