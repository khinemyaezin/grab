package com.grab.app.account.merchant.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.merchant.application.model.write.AutoApproveMerchantApplicationCommand;
import com.merchant.domain.event.MerchantApplicationSubmittedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class MerchantAccountSubmitStatusEventListener extends AbstractIntegrationEventListener {

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public MerchantAccountSubmitStatusEventListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            IdGenerator idGenerator,
            @Qualifier("merchantTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("merchantProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    public void handleMerchantApplicationSubmitted(MerchantApplicationSubmittedEvent event) {
        processDelivery("merchant.ChangeMerchantLifecycle", () -> {
            Id merchantId = idGenerator.convertIdFrom(event.merchantId());
            Id applicantUserId = idGenerator.convertIdFrom(event.applicantUserId());
            AutoApproveMerchantApplicationCommand command =
                    new AutoApproveMerchantApplicationCommand(merchantId, applicantUserId);
            commandBus.dispatch(command);
        });
    }
}
