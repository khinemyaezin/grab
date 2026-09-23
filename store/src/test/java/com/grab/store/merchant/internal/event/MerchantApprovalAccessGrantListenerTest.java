package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;
import com.merchant.domain.event.MerchantApprovedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MerchantApprovalAccessGrantListenerTest {
    private CommandBus commandBus;
    private IdGenerator idGenerator;
    private MerchantApprovalAccessGrantListener listener;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        commandBus = mock(CommandBus.class);
        idGenerator = mock(IdGenerator.class);
        listener = new MerchantApprovalAccessGrantListener(commandBus, idGenerator);

        when(idGenerator.convertIdFrom("mer-1")).thenReturn(new CommonId("mer-1"));
        when(idGenerator.convertIdFrom("usr-1")).thenReturn(new CommonId("usr-1"));
    }

    @Test
    void onMerchantApproved_shouldDispatchProvisionMerchantAdminCommand() {
        MerchantApprovedEvent event = new MerchantApprovedEvent(
                "mer-1", "Test Merchant", "usr-1", "ACTIVE", "reviewer-1", 1, now
        );

        listener.onMerchantApproved(event);

        ArgumentCaptor<ProvisionMerchantAdminCommand> captor = ArgumentCaptor.forClass(ProvisionMerchantAdminCommand.class);
        verify(commandBus).dispatch(captor.capture());

        ProvisionMerchantAdminCommand dispatched = captor.getValue();
        assertThat(dispatched.merchantId().getValue()).isEqualTo("mer-1");
        assertThat(dispatched.applicantUserId().getValue()).isEqualTo("usr-1");
        assertThat(dispatched.occurredAt()).isEqualTo(now);
    }
}
