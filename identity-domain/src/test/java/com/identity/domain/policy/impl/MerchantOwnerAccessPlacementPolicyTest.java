package com.identity.domain.policy.impl;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.service.MerchantAccessProfile;
import com.identity.domain.valueobject.AccessScope;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MerchantOwnerAccessPlacementPolicyTest {
    private final MerchantOwnerAccessPlacementPolicy policy =
            new MerchantOwnerAccessPlacementPolicy();

    @Test
    void roleCode_shouldReturnMerchantOwnerPlacementRoleCode() {
        assertThat(policy.placementRoleCode())
                .isEqualTo(MerchantAccessProfile.OWNER_ROLE_CODE);
    }

    @Test
    void plan_withSellerPlatformAndMerchantScope_shouldDescribeOwnerPlacement() {
        Platform platform = sellerPlatform(
                MerchantAccessProfile.APPLICANT_ROLE_CODE,
                MerchantAccessProfile.OWNER_ROLE_CODE
        );
        AccessScope merchantScope = AccessScope.from(
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                "merchant-123"
        );

        var plan = policy.plan(platform, merchantScope);

        assertThat(plan.previousRoleCode())
                .isEqualTo(MerchantAccessProfile.APPLICANT_ROLE_CODE);
        assertThat(plan.previousScope()).isEqualTo(AccessScope.global());
        assertThat(plan.replacementRoleCode())
                .isEqualTo(MerchantAccessProfile.OWNER_ROLE_CODE);
        assertThat(plan.replacementScope()).isEqualTo(merchantScope);
    }

    @Test
    void plan_withNonSellerPlatform_shouldRejectPlatform() {
        Platform platform = platform(
                "OTHER_PORTAL",
                MerchantAccessProfile.APPLICANT_ROLE_CODE,
                MerchantAccessProfile.OWNER_ROLE_CODE
        );
        AccessScope merchantScope = AccessScope.from(
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                "merchant-123"
        );

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.plan(platform, merchantScope)
        );

        assertInstanceOf(
                IdentityDomainError.InvalidAccessCode.class,
                exception.getMessageSource()
        );
    }

    @Test
    void plan_withGlobalTargetScope_shouldRejectScope() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.plan(
                        sellerPlatform(
                                MerchantAccessProfile.APPLICANT_ROLE_CODE,
                                MerchantAccessProfile.OWNER_ROLE_CODE
                        ),
                        AccessScope.global()
                )
        );

        assertInstanceOf(
                IdentityDomainError.InvalidAccessScope.class,
                exception.getMessageSource()
        );
    }

    @Test
    void plan_withNonMerchantTargetScope_shouldRejectScope() {
        AccessScope otherScope = AccessScope.from("merchant.storefront", "storefront-123");

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.plan(
                        sellerPlatform(
                                MerchantAccessProfile.APPLICANT_ROLE_CODE,
                                MerchantAccessProfile.OWNER_ROLE_CODE
                        ),
                        otherScope
                )
        );

        assertInstanceOf(
                IdentityDomainError.InvalidAccessScope.class,
                exception.getMessageSource()
        );
    }

    @Test
    void plan_whenApplicantRoleIsUnsupported_shouldRejectPlatformRole() {
        Platform platform = sellerPlatform(MerchantAccessProfile.OWNER_ROLE_CODE);

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.plan(platform, merchantScope())
        );

        assertThat(exception.getMessageSource())
                .isEqualTo(new IdentityDomainError.PlatformRoleNotSupported(
                        MerchantAccessProfile.SELLER_PLATFORM_CODE,
                        MerchantAccessProfile.APPLICANT_ROLE_CODE
                ));
    }

    @Test
    void plan_whenOwnerRoleIsUnsupported_shouldRejectPlatformRole() {
        Platform platform = sellerPlatform(MerchantAccessProfile.APPLICANT_ROLE_CODE);

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.plan(platform, merchantScope())
        );

        assertThat(exception.getMessageSource())
                .isEqualTo(new IdentityDomainError.PlatformRoleNotSupported(
                        MerchantAccessProfile.SELLER_PLATFORM_CODE,
                        MerchantAccessProfile.OWNER_ROLE_CODE
                ));
    }

    @Test
    void plan_withNullPlatform_shouldRejectInput() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> policy.plan(null, merchantScope())
        );

        assertThat(exception).hasMessage("platform is required");
    }

    @Test
    void plan_withNullTargetScope_shouldRejectInput() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> policy.plan(
                        sellerPlatform(
                                MerchantAccessProfile.APPLICANT_ROLE_CODE,
                                MerchantAccessProfile.OWNER_ROLE_CODE
                        ),
                        null
                )
        );

        assertThat(exception).hasMessage("targetScope is required");
    }

    private AccessScope merchantScope() {
        return AccessScope.from(
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                "merchant-123"
        );
    }

    private Platform sellerPlatform(String... roleCodes) {
        return platform(MerchantAccessProfile.SELLER_PLATFORM_CODE, roleCodes);
    }

    private Platform platform(String platformCode, String... roleCodes) {
        return new Platform(
                new CommonId("platform-1"),
                platformCode,
                "Platform",
                true,
                Set.of(roleCodes)
        );
    }
}
