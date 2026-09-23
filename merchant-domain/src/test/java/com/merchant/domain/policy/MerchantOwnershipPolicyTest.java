package com.merchant.domain.policy;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.valueobject.MerchantRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantOwnershipPolicyTest {
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");
    private final CommonId merchantId = new CommonId("mer-1");
    private final CommonId memberId = new CommonId("mem-1");
    private final CommonId userId = new CommonId("usr-1");

    private MerchantOwnershipPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new MerchantOwnershipPolicy();
    }

    @Test
    void requireNotSoleAdmin_whenSoleActiveAdmin_shouldThrowException() {
        MerchantMember member = MerchantMember.createAdmin(memberId, merchantId, userId, now);

        assertThatThrownBy(() -> policy.requireNotSoleAdmin(member, 1L))
                .isInstanceOf(MerchantDomainException.class)
                .hasMessageContaining("Cannot demote or remove the sole active admin");
    }

    @Test
    void requireNotSoleAdmin_whenMultipleActiveAdmins_shouldPass() {
        MerchantMember member = MerchantMember.createAdmin(memberId, merchantId, userId, now);

        assertThatCode(() -> policy.requireNotSoleAdmin(member, 2L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireNotSoleAdmin_whenMemberIsNotAdmin_shouldPass() {
        MerchantMember member = MerchantMember.invite(
                memberId, merchantId, userId, MerchantRole.of("OPERATOR"), new CommonId("admin-1"), now.plusSeconds(3600), now
        );

        assertThatCode(() -> policy.requireNotSoleAdmin(member, 1L))
                .doesNotThrowAnyException();
    }
}
