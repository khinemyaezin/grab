package com.grab.store.saleschannel.internal.event;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.saleschannel.internal.query.CheckSalesChannelUsableQuery;
import com.grab.store.saleschannel.internal.query.CheckSalesChannelUsableResult;
import com.grab.store.shared.workflow.FakeModuleOutbox;
import com.grab.store.workflows.events.ChannelAssertedEvent;
import com.grab.store.workflows.events.PublishProductStepFailedEvent;
import com.grab.store.workflows.events.RequestAssertChannelEvent;
import com.saleschannel.infrastructure.workflow.SalesChannelWorkflowStepRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublishProductToChannelSalesChannelEventListenerTest {

    private List<Query<?>> dispatched;
    private FakeModuleOutbox outbox;
    private CheckSalesChannelUsableResult nextResult;
    private PublishProductToChannelSalesChannelEventListener listener;

    @BeforeEach
    void setUp() {
        dispatched = new ArrayList<>();
        outbox = new FakeModuleOutbox();
        nextResult = new CheckSalesChannelUsableResult(true, true, true);
        listener = new PublishProductToChannelSalesChannelEventListener(
                queryBus(),
                new SalesChannelWorkflowStepRunner(outbox.producer(), outbox.transactionManager())
        );
    }

    @Test
    void onRequestAssertChannel_shouldDispatchQueryAndPublishAsserted() {
        listener.onRequestAssertChannel(request());

        assertThat(dispatched).hasSize(1);
        assertThat(dispatched.getFirst()).isInstanceOf(CheckSalesChannelUsableQuery.class);
        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOf(ChannelAssertedEvent.class);
    }

    @Test
    void onRequestAssertChannel_whenNotUsable_shouldFail() {
        nextResult = new CheckSalesChannelUsableResult(true, false, true);

        listener.onRequestAssertChannel(request());

        assertThat(outbox.committed()).hasSize(1);
        assertThat(outbox.committed().getFirst()).isInstanceOfSatisfying(PublishProductStepFailedEvent.class, failed -> {
            assertThat(failed.step()).isEqualTo("assert-channel");
            assertThat(failed.message()).isEqualTo("Sales channel is disabled");
        });
    }

    private RequestAssertChannelEvent request() {
        return new RequestAssertChannelEvent("wf-1", "merchant-1", "channel-1", Instant.now(), 1);
    }

    private QueryBus queryBus() {
        return new QueryBus() {
            @Override
            @SuppressWarnings("unchecked")
            public <R> R dispatch(Query<R> query) {
                dispatched.add(query);
                return (R) nextResult;
            }
        };
    }
}
