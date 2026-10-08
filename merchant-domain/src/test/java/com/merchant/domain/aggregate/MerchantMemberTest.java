package com.merchant.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.enums.MemberStatus;
import com.merchant.domain.event.MerchantMemberCreatedEvent;
import com.merchant.domain.event.MerchantMemberRemovedEvent;
import com.merchant.domain.event.MerchantMemberRoleChangedEvent;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.valueobject.MerchantRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantMemberTest {
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");
    private final CommonId memberId = new CommonId("mem-1");
    private final CommonId merchantId = new CommonId("mer-1");
    private final CommonId userId = new CommonId("usr-1");
    private final CommonId adminUserId = new CommonId("usr-admin");

    @Test
    void createAdmin_shouldCreateActiveAdminAndEmitCreatedEvent() {
        MerchantMember member = MerchantMember.createAdmin(memberId, merchantId, userId, now);

        assertThat(member.getId()).isEqualTo(memberId);
        assertThat(member.getMerchantId()).isEqualTo(merchantId);
        assertThat(member.getUserId()).isEqualTo(userId);
        assertThat(member.getRole()).isEqualTo(MerchantRole.merchantAdmin());
        assertThat(member.isAdmin()).isTrue();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getJoinedAt()).isEqualTo(now);
        assertThat(member.getEvents()).hasSize(1);
        assertThat(member.getEvents().getFirst()).isInstanceOf(MerchantMemberCreatedEvent.class);

        MerchantMemberCreatedEvent event = (MerchantMemberCreatedEvent) member.getEvents().getFirst();
        assertThat(event.role()).isEqualTo("MERCHANT_ADMIN");
        assertThat(event.isAdmin()).isTrue();
        assertThat(event.authorities()).contains("*");
        assertThat(event.status()).isEqualTo("ACTIVE");
        assertThat(event.invitedBy()).isNull();
    }

    @Test
    void invite_shouldCreateInvitedMemberAndEmitCreatedEvent() {
        Instant expiresAt = now.plusSeconds(86400);
        MerchantRole customRole = MerchantRole.of("OPERATOR", Set.of("PRODUCT_READ"));
        MerchantMember member = MerchantMember.invite(
                memberId, merchantId, userId, customRole, adminUserId, expiresAt, now
        );

        assertThat(member.getStatus()).isEqualTo(MemberStatus.INVITED);
        assertThat(member.getRole()).isEqualTo(customRole);
        assertThat(member.isAdmin()).isFalse();
        assertThat(member.getInvitedBy()).isEqualTo(adminUserId);
        assertThat(member.getInvitationExpiresAt()).isEqualTo(expiresAt);
        assertThat(member.getJoinedAt()).isNull();
        assertThat(member.getEvents()).hasSize(1);
        assertThat(member.getEvents().getFirst()).isInstanceOf(MerchantMemberCreatedEvent.class);

        MerchantMemberCreatedEvent event = (MerchantMemberCreatedEvent) member.getEvents().getFirst();
        assertThat(event.role()).isEqualTo("OPERATOR");
        assertThat(event.isAdmin()).isFalse();
        assertThat(event.status()).isEqualTo("INVITED");
        assertThat(event.invitedBy()).isEqualTo(adminUserId.getValue());
    }

    @Test
    void invite_asAdmin_shouldThrowException() {
        assertThatThrownBy(() -> MerchantMember.invite(
                memberId, merchantId, userId, MerchantRole.merchantAdmin(), adminUserId, now.plusSeconds(3600), now
        )).isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void invite_withPastExpiration_shouldThrowException() {
        MerchantRole customRole = MerchantRole.of("OPERATOR");
        assertThatThrownBy(() -> MerchantMember.invite(
                memberId, merchantId, userId, customRole, adminUserId, now.minusSeconds(10), now
        )).isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void accept_byInvitedUser_shouldBecomeActive() {
        Instant expiresAt = now.plusSeconds(86400);
        MerchantRole customRole = MerchantRole.of("OPERATOR");
        MerchantMember member = MerchantMember.invite(
                memberId, merchantId, userId, customRole, adminUserId, expiresAt, now
        );

        Instant acceptedAt = now.plusSeconds(300);
        member.accept(userId, acceptedAt);

        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getJoinedAt()).isEqualTo(acceptedAt);
        assertThat(member.getEvents()).hasSize(2);
        assertThat(member.getEvents().getLast()).isInstanceOf(MerchantMemberRoleChangedEvent.class);

        MerchantMemberRoleChangedEvent roleEvent = (MerchantMemberRoleChangedEvent) member.getEvents().getLast();
        assertThat(roleEvent.previousRole()).isNull();
        assertThat(roleEvent.newRole()).isEqualTo("OPERATOR");
    }

    @Test
    void accept_byDifferentUser_shouldThrowException() {
        Instant expiresAt = now.plusSeconds(86400);
        MerchantRole customRole = MerchantRole.of("OPERATOR");
        MerchantMember member = MerchantMember.invite(
                memberId, merchantId, userId, customRole, adminUserId, expiresAt, now
        );

        CommonId wrongUser = new CommonId("usr-wrong");
        assertThatThrownBy(() -> member.accept(wrongUser, now.plusSeconds(10)))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void accept_whenExpired_shouldThrowException() {
        Instant expiresAt = now.plusSeconds(3600);
        MerchantRole customRole = MerchantRole.of("OPERATOR");
        MerchantMember member = MerchantMember.invite(
                memberId, merchantId, userId, customRole, adminUserId, expiresAt, now
        );

        Instant afterExpiry = expiresAt.plusSeconds(10);
        assertThatThrownBy(() -> member.accept(userId, afterExpiry))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void changeRole_whenActive_shouldUpdateRoleAndEmitEvent() {
        MerchantMember member = MerchantMember.createAdmin(memberId, merchantId, userId, now);

        Instant changedAt = now.plusSeconds(60);
        MerchantRole newRole = MerchantRole.of("ANALYST", Set.of("REPORT_READ"));
        member.changeRole(newRole, changedAt);

        assertThat(member.getRole()).isEqualTo(newRole);
        assertThat(member.getEvents().getLast()).isInstanceOf(MerchantMemberRoleChangedEvent.class);

        MerchantMemberRoleChangedEvent event = (MerchantMemberRoleChangedEvent) member.getEvents().getLast();
        assertThat(event.previousRole()).isEqualTo("MERCHANT_ADMIN");
        assertThat(event.newRole()).isEqualTo("ANALYST");
    }

    @Test
    void changeRole_whenInvited_shouldThrowException() {
        MerchantRole customRole = MerchantRole.of("OPERATOR");
        MerchantMember member = MerchantMember.invite(
                memberId, merchantId, userId, customRole, adminUserId, now.plusSeconds(3600), now
        );

        MerchantRole newRole = MerchantRole.of("ANALYST");
        assertThatThrownBy(() -> member.changeRole(newRole, now.plusSeconds(10)))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void remove_shouldSetRemovedAndEmitEvent() {
        MerchantMember member = MerchantMember.createAdmin(memberId, merchantId, userId, now);

        Instant removedAt = now.plusSeconds(120);
        member.remove(removedAt);

        assertThat(member.getStatus()).isEqualTo(MemberStatus.REMOVED);
        assertThat(member.getEvents().getLast()).isInstanceOf(MerchantMemberRemovedEvent.class);

        MerchantMemberRemovedEvent event = (MerchantMemberRemovedEvent) member.getEvents().getLast();
        assertThat(event.role()).isEqualTo("MERCHANT_ADMIN");
    }
}
