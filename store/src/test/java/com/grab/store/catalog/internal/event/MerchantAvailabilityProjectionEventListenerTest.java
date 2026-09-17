package com.grab.store.catalog.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.catalog.internal.command.UpsertMerchantAvailabilityCommand;
import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import com.grab.store.merchant.events.MerchantClosedIntegrationEvent;
import com.grab.store.merchant.events.MerchantReactivatedIntegrationEvent;
import com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantAvailabilityProjectionEventListenerTest {

    @Test
    void merchantLifecycleEventsUpsertProjectionWithTypeAndStatus() {
        RecordingCommandBus commandBus = new RecordingCommandBus();
        MerchantAvailabilityProjectionEventListener listener =
                new MerchantAvailabilityProjectionEventListener(commandBus);

        listener.onMerchantApproved(new MerchantApprovedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "ACTIVE", Instant.now(), 1));
        assertUpsert("m-1", "ACTIVE", "FIRST_PARTY_RETAILER", commandBus.last);

        listener.onMerchantSuspended(new MerchantSuspendedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "SUSPENDED", Instant.now(), 2));
        assertUpsert("m-1", "SUSPENDED", "FIRST_PARTY_RETAILER", commandBus.last);

        listener.onMerchantReactivated(new MerchantReactivatedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "ACTIVE", Instant.now(), 3));
        assertUpsert("m-1", "ACTIVE", "FIRST_PARTY_RETAILER", commandBus.last);

        listener.onMerchantClosed(new MerchantClosedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "CLOSED", Instant.now(), 4));
        assertUpsert("m-1", "CLOSED", "FIRST_PARTY_RETAILER", commandBus.last);
    }

    private static void assertUpsert(String merchantId, String status, String merchantType, Command<?> command) {
        assertThat(command).isInstanceOf(UpsertMerchantAvailabilityCommand.class);
        UpsertMerchantAvailabilityCommand upsert = (UpsertMerchantAvailabilityCommand) command;
        assertThat(upsert.merchantId()).isEqualTo(merchantId);
        assertThat(upsert.status()).isEqualTo(status);
        assertThat(upsert.merchantType()).isEqualTo(merchantType);
    }

    private static final class RecordingCommandBus implements CommandBus {
        private Command<?> last;

        @Override
        public <R> R dispatch(Command<R> command) {
            last = command;
            return null;
        }
    }
}
