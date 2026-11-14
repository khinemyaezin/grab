package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ChangeMerchantLifecycleUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChangeMerchantLifecycleCommandHandler
        implements CommandHandler<ChangeMerchantLifecycleCommand, MerchantAccountResult> {

    static final String CONSUMER = "merchant.ChangeMerchantLifecycle";

    private final ChangeMerchantLifecycleUseCase changeMerchantLifecycleUseCase;
    private final ProcessedEventRepository processedEvents;

    public ChangeMerchantLifecycleCommandHandler(
            ChangeMerchantLifecycleUseCase changeMerchantLifecycleUseCase,
            @Qualifier("merchantProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        this.changeMerchantLifecycleUseCase = changeMerchantLifecycleUseCase;
        this.processedEvents = processedEvents;
    }

    /**
     * Returns {@code null} when the triggering outbox event was already applied (duplicate delivery).
     */
    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(ChangeMerchantLifecycleCommand command) {
        UUID sourceEventId = command.sourceEventId();
        if (sourceEventId != null && !processedEvents.markProcessed(CONSUMER, sourceEventId)) {
            return null;
        }
        return changeMerchantLifecycleUseCase.execute(command);
    }

    @Override
    public Class<ChangeMerchantLifecycleCommand> getCommandType() {
        return ChangeMerchantLifecycleCommand.class;
    }
}
