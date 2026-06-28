package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.mapper.ChangeMerchantLifecycleRequestMapper;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand.Action;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantCommandServiceTest {
    @Test
    void changeLifecycle_withApproveAction_shouldMapAndDispatchRequestedAction() {
        AtomicReference<Command<?>> dispatched = new AtomicReference<>();
        CommandBus commands = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                dispatched.set(command);
                return null;
            }
        };
        ChangeMerchantLifecycleRequestMapper lifecycleMapper = new ChangeMerchantLifecycleRequestMapper() {
            @Override
            public ChangeMerchantLifecycleCommand toCommand(
                    String merchantId, String actorId, Action action, MerchantLifecycleRequest request
            ) {
                return new ChangeMerchantLifecycleCommand(
                        new CommonId(merchantId), new CommonId(actorId), action, request.reason());
            }

            @Override
            public com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse toResponse(
                    MerchantAccountResult result
            ) {
                return null;
            }
        };
        MerchantCommandService service = new MerchantCommandService(
                commands,
                null,
                null,
                null,
                lifecycleMapper
        );

        service.changeLifecycle("merchant-1", "reviewer-1", Action.APPROVE, null);

        ChangeMerchantLifecycleCommand command = (ChangeMerchantLifecycleCommand) dispatched.get();
        assertThat(command.merchantId().getValue()).isEqualTo("merchant-1");
        assertThat(command.actorId().getValue()).isEqualTo("reviewer-1");
        assertThat(command.action()).isEqualTo(Action.APPROVE);
    }
}
