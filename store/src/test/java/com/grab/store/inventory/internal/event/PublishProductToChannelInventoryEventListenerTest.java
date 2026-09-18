package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.inventory.internal.query.CheckChannelStockPathQuery;
import com.grab.store.inventory.internal.query.CheckChannelStockPathResult;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.RequestCheckChannelStockPathEvent;
import com.grab.store.workflows.events.StockPathCheckedEvent;
import com.inventory.infrastructure.workflow.InventoryWorkflowStepRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublishProductToChannelInventoryEventListenerTest {

    private List<Query<?>> dispatched;
    private FakeModuleOutbox outbox;
    private boolean routeExists;
    private PublishProductToChannelInventoryEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        routeExists = false;
        listener = new PublishProductToChannelInventoryEventListener(
                queryBus(),
                new InventoryWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    @Test
    void onRequestCheckChannelStockPath_neverFailsAndFlagsMissingRoute() {
        listener.onRequestCheckChannelStockPath(new RequestCheckChannelStockPathEvent(
                "wf-1", "merchant-1", "channel-1", Instant.now(), 1
        ));

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CheckChannelStockPathQuery.class);
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(StockPathCheckedEvent.class, event -> {
            assertThat(event.missingRoute()).isTrue();
            assertThat(event.workflowId()).isEqualTo("wf-1");
        });
    }

    @Test
    void onRequestCheckChannelStockPath_whenRouteExists_missingRouteIsFalse() {
        routeExists = true;

        listener.onRequestCheckChannelStockPath(new RequestCheckChannelStockPathEvent(
                "wf-1", "merchant-1", "channel-1", Instant.now(), 1
        ));

        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(StockPathCheckedEvent.class, event ->
                assertThat(event.missingRoute()).isFalse());
    }

    private QueryBus queryBus() {
        return new QueryBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Query<R> query) {
                dispatched.add(query);
                return (R) new CheckChannelStockPathResult(routeExists);
            }
        };
    }
}
