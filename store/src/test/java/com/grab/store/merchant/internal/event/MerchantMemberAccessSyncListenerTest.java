package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.RevokeMerchantMemberAccessCommand;
import com.merchant.application.model.write.SyncMerchantMemberRoleAccessCommand;
import com.merchant.domain.event.MerchantMemberRemovedEvent;
import com.merchant.domain.event.MerchantMemberRoleChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantMemberAccessSyncListenerTest {
    private CommandBus commandBus;
    private IdGenerator idGenerator;
    private MerchantMemberAccessSyncListener listener;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        commandBus = mock(CommandBus.class);
        idGenerator = mock(IdGenerator.class);
        when(idGenerator.convertIdFrom(anyString())).thenAnswer(invocation -> new CommonId(invocation.getArgument(0)));
        listener = new MerchantMemberAccessSyncListener(commandBus, idGenerator);
    }

    @Test
    void onMemberRoleChanged_whenInvoked_dispatchesSyncMerchantMemberRoleAccessCommand() {
        MerchantMemberRoleChangedEvent event = new MerchantMemberRoleChangedEvent(
                "mem-1", "mer-1", "usr-1", "OPERATOR", "ANALYST", Set.of("INVENTORY_READ"), 2, now
        );

        listener.onMemberRoleChanged(event);

        ArgumentCaptor<SyncMerchantMemberRoleAccessCommand> captor =
                ArgumentCaptor.forClass(SyncMerchantMemberRoleAccessCommand.class);
        verify(commandBus).dispatch(captor.capture());

        SyncMerchantMemberRoleAccessCommand command = captor.getValue();
        assertThat(command.merchantId()).isEqualTo(new CommonId("mer-1"));
        assertThat(command.memberId()).isEqualTo(new CommonId("mem-1"));
        assertThat(command.userId()).isEqualTo(new CommonId("usr-1"));
        assertThat(command.previousRole()).isEqualTo("OPERATOR");
        assertThat(command.newRole()).isEqualTo("ANALYST");
        assertThat(command.authorities()).containsExactly("INVENTORY_READ");
    }

    @Test
    void onMemberRemoved_whenInvoked_dispatchesRevokeMerchantMemberAccessCommand() {
        MerchantMemberRemovedEvent event = new MerchantMemberRemovedEvent(
                "mem-1", "mer-1", "usr-1", "OPERATOR", 3, now
        );

        listener.onMemberRemoved(event);

        ArgumentCaptor<RevokeMerchantMemberAccessCommand> captor =
                ArgumentCaptor.forClass(RevokeMerchantMemberAccessCommand.class);
        verify(commandBus).dispatch(captor.capture());

        RevokeMerchantMemberAccessCommand command = captor.getValue();
        assertThat(command.merchantId()).isEqualTo(new CommonId("mer-1"));
        assertThat(command.memberId()).isEqualTo(new CommonId("mem-1"));
        assertThat(command.userId()).isEqualTo(new CommonId("usr-1"));
        assertThat(command.role()).isEqualTo("OPERATOR");
    }
}
