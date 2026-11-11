package com.grab.app.merchandising.inventory.internal.adapter.inbound.event;

import com.grab.app.workflow.port.event.RequestCheckChannelStockPathEvent;
import com.grab.app.workflow.port.event.StockPathCheckedEvent;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.domain.Event;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.adapter.persistence.workflow.InventoryWorkflowStepRunner;
import com.inventory.application.model.read.CheckChannelStockPathQuery;
import com.inventory.application.model.read.CheckChannelStockPathResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PublishProductToChannelInventoryEventListener {

    private static final Logger log = Loggers.getLogger(PublishProductToChannelInventoryEventListener.class);
    private static final int EVENT_VERSION = 1;

    private final QueryBus queryBus;
    private final InventoryWorkflowStepRunner signalEmitter;

    public PublishProductToChannelInventoryEventListener(
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            InventoryWorkflowStepRunner signalEmitter
    ) {
        this.queryBus = queryBus;
        this.signalEmitter = signalEmitter;
    }

    @EventListener
    public void onRequestCheckChannelStockPath(RequestCheckChannelStockPathEvent event) {
        log.info(
                "Handling RequestCheckChannelStockPathEvent workflowId={} salesChannelId={}",
                event.workflowId(),
                event.salesChannelId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> checkStockPath(event),
                exception -> {
                    log.warn(
                            "Check channel stock path failed for workflowId={}: {}",
                            event.workflowId(),
                            exception.getMessage()
                    );
                    return List.of(new StockPathCheckedEvent(
                            event.workflowId(),
                            event.salesChannelId(),
                            true,
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    private List<Event> checkStockPath(RequestCheckChannelStockPathEvent event) {
        CheckChannelStockPathResult result = queryBus.dispatch(new CheckChannelStockPathQuery(
                event.merchantId(),
                event.salesChannelId()
        ));
        return List.of(new StockPathCheckedEvent(
                event.workflowId(),
                event.salesChannelId(),
                !result.routeExists(),
                Instant.now(),
                EVENT_VERSION
        ));
    }
}

