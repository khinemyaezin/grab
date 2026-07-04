package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.identity.internal.command.ReplaceMerchantApplicantAccessCommand;
import com.merchant.domain.event.MerchantApprovedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantAccountApprovedStatusEventListenerTest {
    @Test
    void handleMerchantApproved_withApprovedEvent_shouldDispatchAccessReplacement() {
        MerchantApprovedEvent event = new MerchantApprovedEvent(
                "event-1",
                "merchant-1",
                "applicant-1",
                "ACTIVE",
                "reviewer-1",
                2,
                Instant.parse("2026-07-02T00:00:00Z")
        );
        RecordingCommandBus commandBus = new RecordingCommandBus();
        MerchantAccountApprovedStatusEventListener listener = new MerchantAccountApprovedStatusEventListener(
                commandBus,
                new ConvertingIdGenerator()
        );

        listener.handleMerchantApproved(event);

        assertThat(commandBus.dispatched).isInstanceOf(ReplaceMerchantApplicantAccessCommand.class);
        ReplaceMerchantApplicantAccessCommand command =
                (ReplaceMerchantApplicantAccessCommand) commandBus.dispatched;
        assertThat(command.applicantUserId().getValue()).isEqualTo("applicant-1");
        assertThat(command.merchantId().getValue()).isEqualTo("merchant-1");
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
