package com.grab.store.workflows.publishproducttochannel;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.impl.EventDrivenWorkflowEngine;
import com.grab.framework.workflow.impl.InMemoryWorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.workflows.events.ChannelAssertedEvent;
import com.grab.store.workflows.events.ProductAssertedEvent;
import com.grab.store.workflows.events.ProductPublishedToChannelEvent;
import com.grab.store.workflows.events.ProductUnpublishedFromChannelEvent;
import com.grab.store.workflows.events.PublishProductStepFailedEvent;
import com.grab.store.workflows.events.RequestAssertChannelEvent;
import com.grab.store.workflows.events.RequestAssertProductEvent;
import com.grab.store.workflows.events.RequestCheckChannelStockPathEvent;
import com.grab.store.workflows.events.RequestUnpublishProductCompensationEvent;
import com.grab.store.workflows.events.RequestWritePublicationEvent;
import com.grab.store.workflows.events.StockPathCheckedEvent;
import com.grab.store.workflows.internal.service.WorkflowTerminalLifecycleListener;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.PublishProductToChannelContext;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.PublishProductToChannelDefinition;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.PublishProductToChannelTerminalContextAdapter;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.PublishProductToChannelWorkflowNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublishProductToChannelDefinitionTest {

    private InMemoryWorkflowStore workflowStore;
    private List<Object> published;
    private EventDrivenWorkflowEngine engine;
    private ProcessDefinition<PublishProductToChannelContext> definition;
    private WorkflowPayloadCodec codec;

    @BeforeEach
    void setUp() {
        workflowStore = new InMemoryWorkflowStore();
        published = new ArrayList<>();
        ApplicationEventPublisher events = published::add;
        codec = new WorkflowPayloadCodec();
        definition = new PublishProductToChannelDefinition().create();
        engine = new EventDrivenWorkflowEngine(
                workflowStore,
                new WorkflowDefinitionRegistry(List.of(definition)),
                codec,
                (workflowId, eventsToPublish) -> published.addAll(eventsToPublish),
                new IdGenerator() {
                    private int counter;

                    @Override
                    public Id generateId() {
                        return new CommonId("wf-" + (++counter));
                    }

                    @Override
                    public Id convertIdFrom(String id) {
                        return new CommonId(id);
                    }
                },
                new WorkflowTerminalLifecycleListener(
                        events,
                        List.of(new PublishProductToChannelTerminalContextAdapter())
                )
        );
    }

    @Test
    void start_shouldRequestAssertChannel() {
        WorkflowInstance instance = engine.start(definition, sampleContext(), "idem-1");

        assertThat(instance.status()).isEqualTo(WorkflowStatus.WAITING_EXTERNAL);
        assertThat(instance.currentStep()).contains(PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL);
        assertThat(published.getFirst()).isInstanceOf(RequestAssertChannelEvent.class);
    }

    @Test
    void happyPath_shouldWritePublicationAfterWarnOnlyStockPath() {
        WorkflowInstance started = engine.start(definition, sampleContext(), "idem-ok");
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL,
                new ChannelAssertedEvent(started.id(), "channel-1", Instant.now(), 1),
                "channel-asserted:channel-1"
        ));
        assertThat(published.getFirst()).isInstanceOf(RequestAssertProductEvent.class);
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                PublishProductToChannelWorkflowNames.STEP_ASSERT_PRODUCT,
                new ProductAssertedEvent(started.id(), "prod-1", Instant.now(), 1),
                "product-asserted:prod-1"
        ));
        assertThat(published.getFirst()).isInstanceOf(RequestCheckChannelStockPathEvent.class);
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH,
                new StockPathCheckedEvent(started.id(), "channel-1", true, Instant.now(), 1),
                "stock-path-checked:channel-1"
        ));
        assertThat(published.getFirst()).isInstanceOf(RequestWritePublicationEvent.class);

        engine.onSignal(InboundSignal.completion(
                started.id(),
                PublishProductToChannelWorkflowNames.STEP_WRITE_PUBLICATION,
                new ProductPublishedToChannelEvent(started.id(), "prod-1", "channel-1", Instant.now(), 1),
                "product-published-to-channel:prod-1:channel-1"
        ));

        WorkflowInstance completed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
        PublishProductToChannelContext context = codec.readTyped(
                completed.contextJson().orElseThrow(),
                PublishProductToChannelContext.class
        );
        assertThat(context.missingRoute()).isTrue();
        assertThat(context.publicationWritten()).isTrue();
    }

    @Test
    void channelFailure_shouldNotCompensate() {
        WorkflowInstance started = engine.start(definition, sampleContext(), null);
        published.clear();

        engine.onSignal(InboundSignal.failure(
                started.id(),
                PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL,
                new PublishProductStepFailedEvent(
                        started.id(),
                        PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL,
                        "Sales channel is disabled",
                        Instant.now(),
                        1
                ),
                "failed:assert-channel:Sales channel is disabled"
        ));

        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).noneMatch(RequestUnpublishProductCompensationEvent.class::isInstance);
    }

    @Test
    void productFailure_shouldNotCompensate() {
        WorkflowInstance started = engine.start(definition, sampleContext(), null);
        engine.onSignal(InboundSignal.completion(
                started.id(),
                PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL,
                new ChannelAssertedEvent(started.id(), "channel-1", Instant.now(), 1),
                "channel-asserted:channel-1"
        ));
        published.clear();

        engine.onSignal(InboundSignal.failure(
                started.id(),
                PublishProductToChannelWorkflowNames.STEP_ASSERT_PRODUCT,
                new PublishProductStepFailedEvent(
                        started.id(),
                        PublishProductToChannelWorkflowNames.STEP_ASSERT_PRODUCT,
                        "Product must be ACTIVE to publish to a sales channel",
                        Instant.now(),
                        1
                ),
                "failed:assert-product:Product must be ACTIVE to publish to a sales channel"
        ));

        WorkflowInstance failed = workflowStore.findById(started.id()).orElseThrow();
        assertThat(failed.status()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(published).noneMatch(RequestUnpublishProductCompensationEvent.class::isInstance);
    }

    @Test
    void writeStepCompensate_shouldUnpublishWhenWritten() {
        PublishProductToChannelContext written = sampleContext()
                .withChannelAsserted()
                .withProductAsserted()
                .withStockPathChecked(false)
                .withPublicationWritten();

        var events = definition.step(PublishProductToChannelWorkflowNames.STEP_WRITE_PUBLICATION)
                .orElseThrow()
                .compensate("wf-1", written);

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOfSatisfying(RequestUnpublishProductCompensationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("prod-1");
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
        });

        PublishProductToChannelContext compensated = definition
                .step(PublishProductToChannelWorkflowNames.STEP_WRITE_PUBLICATION)
                .orElseThrow()
                .onCompensationAck(
                        written,
                        InboundSignal.compensationAck(
                                "wf-1",
                                new ProductUnpublishedFromChannelEvent(
                                        "wf-1", "prod-1", "channel-1", Instant.now(), 1),
                                "product-unpublished-from-channel:prod-1:channel-1"
                        )
                );
        assertThat(compensated.publicationCompensated()).isTrue();
    }

    private static PublishProductToChannelContext sampleContext() {
        return PublishProductToChannelContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "prod-1",
                "channel-1"
        );
    }
}
