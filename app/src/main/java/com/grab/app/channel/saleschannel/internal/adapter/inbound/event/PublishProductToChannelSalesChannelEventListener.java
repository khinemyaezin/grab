package com.grab.app.channel.saleschannel.internal.adapter.inbound.event;

import com.grab.app.workflow.port.event.ChannelAssertedEvent;
import com.grab.app.workflow.port.event.PublishProductStepFailedEvent;
import com.grab.app.workflow.port.event.RequestAssertChannelEvent;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.domain.Event;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.saleschannel.adapter.persistence.workflow.SalesChannelWorkflowStepRunner;
import com.saleschannel.application.model.read.CheckSalesChannelUsableQuery;
import com.saleschannel.application.model.read.CheckSalesChannelUsableResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PublishProductToChannelSalesChannelEventListener {

    private static final Logger log = Loggers.getLogger(PublishProductToChannelSalesChannelEventListener.class);
    private static final int EVENT_VERSION = 1;

    private final QueryBus queryBus;
    private final SalesChannelWorkflowStepRunner signalEmitter;

    public PublishProductToChannelSalesChannelEventListener(
            @Qualifier("channelQueryBus") QueryBus queryBus,
            SalesChannelWorkflowStepRunner signalEmitter
    ) {
        this.queryBus = queryBus;
        this.signalEmitter = signalEmitter;
    }

    @EventListener
    public void onRequestAssertChannel(RequestAssertChannelEvent event) {
        log.info(
                "Handling RequestAssertChannelEvent workflowId={} salesChannelId={}",
                event.workflowId(),
                event.salesChannelId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> assertChannel(event),
                exception -> {
                    log.warn(
                            "Assert channel failed for workflowId={}: {}",
                            event.workflowId(),
                            exception.getMessage()
                    );
                    return List.of(new PublishProductStepFailedEvent(
                            event.workflowId(),
                            "assert-channel",
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    private List<Event> assertChannel(RequestAssertChannelEvent event) {
        CheckSalesChannelUsableResult result = queryBus.dispatch(new CheckSalesChannelUsableQuery(
                event.salesChannelId(),
                event.merchantId()
        ));
        if (!result.usable()) {
            throw new IllegalStateException(failureMessage(result));
        }
        return List.of(new ChannelAssertedEvent(
                event.workflowId(),
                event.salesChannelId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private String failureMessage(CheckSalesChannelUsableResult result) {
        if (!result.found()) {
            return "Sales channel not found";
        }
        if (!result.enabled()) {
            return "Sales channel is disabled";
        }
        return "Merchant does not own this sales channel";
    }
}

