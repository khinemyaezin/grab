package com.grab.service.account.merchant.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.merchant.application.model.write.AutoApproveMerchantApplicationCommand;
import com.merchant.domain.event.MerchantApplicationSubmittedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MerchantAccountSubmitStatusEventListener {

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public MerchantAccountSubmitStatusEventListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            IdGenerator idGenerator
    ) {
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    public void handleMerchantApplicationSubmitted(MerchantApplicationSubmittedEvent event) {
        Id merchantId = idGenerator.convertIdFrom(event.merchantId());
        Id applicantUserId = idGenerator.convertIdFrom(event.applicantUserId());
        AutoApproveMerchantApplicationCommand command =
                new AutoApproveMerchantApplicationCommand(merchantId, applicantUserId);
        commandBus.dispatch(command);
    }
}
