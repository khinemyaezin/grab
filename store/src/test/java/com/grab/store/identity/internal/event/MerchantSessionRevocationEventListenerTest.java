package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.merchant.events.MerchantClosedIntegrationEvent;
import com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent;
import com.grab.store.merchant.events.StorefrontStatusChangedIntegrationEvent;
import com.grab.store.shared.security.PlatformScopes;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantSessionRevocationEventListenerTest {

    @Test
    void merchantSuspendAndCloseRevokeSellerPortalAccountSessions() {
        RecordingCommandBus commandBus = new RecordingCommandBus();
        MerchantSessionRevocationEventListener listener = new MerchantSessionRevocationEventListener(commandBus);

        listener.onMerchantSuspended(new MerchantSuspendedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "SUSPENDED", Instant.now(), 1));
        listener.onMerchantClosed(new MerchantClosedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "CLOSED", Instant.now(), 2));

        assertThat(commandBus.dispatched).containsExactly(
                new RevokeSessionsByScopeCommand(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "m-1"),
                new RevokeSessionsByScopeCommand(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "m-1")
        );
    }

    @Test
    void storefrontSuspendRevokesStorefrontScopeAndActivateIsNoOp() {
        RecordingCommandBus commandBus = new RecordingCommandBus();
        MerchantSessionRevocationEventListener listener = new MerchantSessionRevocationEventListener(commandBus);

        listener.onStorefrontStatusChanged(new StorefrontStatusChangedIntegrationEvent(
                "sf-1", "m-1", "shop", "DRAFT", "ACTIVE", Instant.now(), 1));
        listener.onStorefrontStatusChanged(new StorefrontStatusChangedIntegrationEvent(
                "sf-1", "m-1", "shop", "ACTIVE", "SUSPENDED", Instant.now(), 2));
        listener.onStorefrontStatusChanged(new StorefrontStatusChangedIntegrationEvent(
                "sf-1", "m-1", "shop", "SUSPENDED", "CLOSED", Instant.now(), 3));

        assertThat(commandBus.dispatched).containsExactly(
                new RevokeSessionsByScopeCommand(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_STOREFRONT_SCOPE, "sf-1"),
                new RevokeSessionsByScopeCommand(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_STOREFRONT_SCOPE, "sf-1")
        );
    }

    private static final class RecordingCommandBus implements CommandBus {
        private final List<Command<?>> dispatched = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public <R> R dispatch(Command<R> command) {
            dispatched.add(command);
            return null;
        }
    }
}
