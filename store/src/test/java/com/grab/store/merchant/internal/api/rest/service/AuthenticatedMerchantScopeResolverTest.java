package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.grab.store.shared.security.SecurityPrincipal;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedMerchantScopeResolverTest {
    private final AuthenticatedMerchantScopeResolver resolver = new AuthenticatedMerchantScopeResolver();

    @Test
    void resolveCurrentMerchantId_withMerchantAccountContext_shouldReturnScopeId() {
        SecurityPrincipal principal = principal(new AccessContext(
                "SELLER_PORTAL", "assignment-1", "merchant.account", "merchant-1"));

        String merchantId = resolver.resolveCurrentMerchantId(principal);

        assertThat(merchantId).isEqualTo("merchant-1");
    }

    @Test
    void resolveCurrentMerchantId_withoutContext_shouldRejectAccess() {
        SecurityPrincipal principal = principal(null);

        assertThatThrownBy(() -> resolver.resolveCurrentMerchantId(principal))
                .isInstanceOf(MerchantServiceException.class)
                .satisfies(error -> assertThat(((MerchantServiceException) error)
                        .getMessageSource().code()).isEqualTo("mer.service.scope.forbidden"));
    }

    @Test
    void resolveCurrentMerchantId_withNonMerchantScope_shouldRejectAccess() {
        SecurityPrincipal principal = principal(new AccessContext(
                "SELLER_PORTAL", "assignment-1", "merchant.storefront", "storefront-1"));

        assertThatThrownBy(() -> resolver.resolveCurrentMerchantId(principal))
                .isInstanceOf(MerchantServiceException.class);
    }

    @Test
    void resolveScopedAccess_withDifferentSelectedMerchant_shouldRejectAccess() {
        SecurityPrincipal principal = principal(new AccessContext(
                "SELLER_PORTAL", "assignment-1", "merchant.account", "merchant-1"));

        assertThatThrownBy(() -> resolver.resolveScopedAccess(principal, "merchant-2", false))
                .isInstanceOf(MerchantServiceException.class);
    }

    private SecurityPrincipal principal(AccessContext context) {
        AuthenticatedActor actor = new AuthenticatedActor(
                "user-1",
                "local",
                "user-1",
                "user@example.com",
                Set.of("MERCHANT_OWNER"),
                Set.of("MERCHANT_PROFILE_READ"),
                context
        );
        return new SecurityPrincipal(actor);
    }
}
