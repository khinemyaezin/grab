package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.identity.internal.command.ReplaceAccessCommand;
import com.grab.store.identity.internal.policy.impl.DefaultMerchantApprovalAccessPolicy;
import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantAccountApprovedStatusEventListenerTest {
    @Test
    void handleMerchantApproved_withApprovedEvent_shouldDispatchAccessReplacement() {
        MerchantApprovedIntegrationEvent event = new MerchantApprovedIntegrationEvent(
                "event-1",
                "merchant-1",
                "applicant-1",
                Instant.parse("2026-07-02T00:00:00Z"),
                1
        );
        RecordingCommandBus commandBus = new RecordingCommandBus();
        MerchantAccountApprovedStatusEventListener listener = new MerchantAccountApprovedStatusEventListener(
                commandBus,
                new ConvertingIdGenerator(),
                new DefaultMerchantApprovalAccessPolicy()
        );

        listener.handleMerchantApproved(event);

        assertThat(commandBus.dispatched).isInstanceOf(ReplaceAccessCommand.class);
        ReplaceAccessCommand command = (ReplaceAccessCommand) commandBus.dispatched;
        assertThat(command.userId().getValue()).isEqualTo("applicant-1");
        assertThat(command.platformCode()).isEqualTo("SELLER_PORTAL");
        assertThat(command.replacementRoleCode()).isEqualTo("MERCHANT_OWNER");
        assertThat(command.scopeKey()).isEqualTo("merchant.account");
        assertThat(command.scopeId()).isEqualTo("merchant-1");
    }

    private static final class RecordingCommandBus implements CommandBus {
        private Command<?> dispatched;

        @Override
        public <R> R dispatch(Command<R> command) {
            dispatched = command;
            return null;
        }
    }

    private static final class ConvertingIdGenerator implements IdGenerator {
        @Override
        public Id generateId() {
            return new CommonId("generated-id");
        }

        @Override
        public Id convertIdFrom(String id) {
            return new CommonId(id);
        }
    }
}
