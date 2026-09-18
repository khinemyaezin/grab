package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.domain.Event;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.query.CheckChannelStockPathQuery;
import com.grab.store.inventory.internal.query.CheckChannelStockPathResult;
import com.grab.store.workflows.events.RequestCheckChannelStockPathEvent;
import com.grab.store.workflows.events.StockPathCheckedEvent;
import com.inventory.infrastructure.workflow.InventoryWorkflowStepRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PublishProductToChannelInventoryEventListener {

    private static final Logger log = Loggers.getLogger(PublishProductToChannelInventoryEventListener.class);
    private static final int EVENT_VERSION = 1;

    private final QueryBus queryBus;
    private final InventoryWorkflowStepRunner signalEmitter;

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
