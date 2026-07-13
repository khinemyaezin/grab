package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.security.AccessContext;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.SecurityPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedInventoryScopeResolverTest {

    private AuthenticatedInventoryScopeResolver resolver;

    @Mock
    private SecurityPrincipal securityPrincipal;

    @BeforeEach
    void setUp() {
        resolver = new AuthenticatedInventoryScopeResolver();
    }

    private void mockSecurityPrincipal(String platformCode, String scopeKey, String scopeId, String platformUserId) {
        AccessContext context = new AccessContext(platformCode, "assignment-1", scopeKey, scopeId);
        lenient().when(securityPrincipal.getAccessContext()).thenReturn(Optional.of(context));
        lenient().when(securityPrincipal.getPlatformUserId()).thenReturn(platformUserId);
    }

    private void mockEmptySecurityPrincipal() {
        when(securityPrincipal.getAccessContext()).thenReturn(Optional.empty());
    }

    @Nested
    class ResolveOwnerMerchantId {

        @Test
        void resolveOwnerMerchantId_withMerchantScope_shouldReturnMerchantId() {
            mockSecurityPrincipal(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "merchant-123", "user-1");
            String merchantId = resolver.resolveOwnerMerchantId(securityPrincipal);
            assertThat(merchantId).isEqualTo("merchant-123");
        }

        @Test
        void resolveOwnerMerchantId_withOtherScope_shouldThrow() {
            mockSecurityPrincipal(PlatformScopes.SELLER_PORTAL, "other.scope", "merchant-123", "user-1");
            assertThatThrownBy(() -> resolver.resolveOwnerMerchantId(securityPrincipal))
                    .isInstanceOf(InventoryServiceException.class);
        }

        @Test
        void resolveOwnerMerchantId_withNoContext_shouldThrow() {
            mockEmptySecurityPrincipal();
            assertThatThrownBy(() -> resolver.resolveOwnerMerchantId(securityPrincipal))
                    .isInstanceOf(InventoryServiceException.class);
        }
    }

    @Nested
    class Resolve {

        @Test
        void resolve_withMerchantAccountScope_shouldReturnAccess() {
            mockSecurityPrincipal(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "merchant-123", "user-1");

            ResolvedInventoryAccess access = resolver.resolve(securityPrincipal);

            assertThat(access.actorId()).isEqualTo("user-1");
            assertThat(access.scopeKey()).isEqualTo(PlatformScopes.MERCHANT_ACCOUNT_SCOPE);
            assertThat(access.scopeId()).isEqualTo("merchant-123");
        }

        @Test
        void resolve_withFulfillmentLocationScope_shouldReturnAccess() {
            mockSecurityPrincipal(PlatformScopes.SELLER_PORTAL, PlatformScopes.FULFILLMENT_LOCATION_SCOPE, "location-456", "user-1");

            ResolvedInventoryAccess access = resolver.resolve(securityPrincipal);

            assertThat(access.actorId()).isEqualTo("user-1");
            assertThat(access.scopeKey()).isEqualTo(PlatformScopes.FULFILLMENT_LOCATION_SCOPE);
            assertThat(access.scopeId()).isEqualTo("location-456");
        }

        @Test
        void resolve_withOtherScope_shouldThrowException() {
            mockSecurityPrincipal(PlatformScopes.SELLER_PORTAL, "some.other.scope", "value", "user-1");
            assertThatThrownBy(() -> resolver.resolve(securityPrincipal))
                    .isInstanceOf(InventoryServiceException.class);
        }

        @Test
        void resolve_withNoContext_shouldThrowException() {
            mockEmptySecurityPrincipal();
            assertThatThrownBy(() -> resolver.resolve(securityPrincipal))
                    .isInstanceOf(InventoryServiceException.class);
        }
    }


}
