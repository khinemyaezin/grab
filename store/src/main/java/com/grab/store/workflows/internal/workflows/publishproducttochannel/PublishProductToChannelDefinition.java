package com.grab.store.workflows.internal.workflows.publishproducttochannel;

import com.grab.framework.domain.Event;
import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.StepDefinition;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.store.workflows.events.ChannelAssertedEvent;
import com.grab.store.workflows.events.ProductAssertedEvent;
import com.grab.store.workflows.events.ProductPublishedToChannelEvent;
import com.grab.store.workflows.events.ProductUnpublishedFromChannelEvent;
import com.grab.store.workflows.events.RequestAssertChannelEvent;
import com.grab.store.workflows.events.RequestAssertProductEvent;
import com.grab.store.workflows.events.RequestCheckChannelStockPathEvent;
import com.grab.store.workflows.events.RequestUnpublishProductCompensationEvent;
import com.grab.store.workflows.events.RequestWritePublicationEvent;
import com.grab.store.workflows.events.StockPathCheckedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public final class PublishProductToChannelDefinition implements WorkflowProcess<PublishProductToChannelContext> {

    private static final int EVENT_VERSION = 1;

    private final ProcessDefinition<PublishProductToChannelContext> definition = ProcessDefinition.of(
            PublishProductToChannelWorkflowNames.WORKFLOW_NAME,
            PublishProductToChannelContext.class,
            new AssertChannelStep(),
            new AssertProductStep(),
            new AssertChannelStockPathStep(),
            new WritePublicationStep()
    );

    @Override
    public ProcessDefinition<PublishProductToChannelContext> create() {
        return definition;
    }

    private static final class AssertChannelStep implements StepDefinition<PublishProductToChannelContext> {
        @Override
        public String name() {
            return PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL;
        }

        @Override
        public List<Event> onEnter(String workflowId, PublishProductToChannelContext context) {
            return List.of(new RequestAssertChannelEvent(
                    workflowId,
                    context.merchantId(),
                    context.salesChannelId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public PublishProductToChannelContext onSignal(PublishProductToChannelContext context, InboundSignal signal) {
            if (signal.event() instanceof ChannelAssertedEvent) {
                return context.withChannelAsserted();
            }
            return context;
        }

        @Override
        public boolean isComplete(PublishProductToChannelContext context) {
            return context.channelAsserted();
        }
    }

    private static final class AssertProductStep implements StepDefinition<PublishProductToChannelContext> {
        @Override
        public String name() {
            return PublishProductToChannelWorkflowNames.STEP_ASSERT_PRODUCT;
        }

        @Override
        public List<Event> onEnter(String workflowId, PublishProductToChannelContext context) {
            return List.of(new RequestAssertProductEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public PublishProductToChannelContext onSignal(PublishProductToChannelContext context, InboundSignal signal) {
            if (signal.event() instanceof ProductAssertedEvent) {
                return context.withProductAsserted();
            }
            return context;
        }

        @Override
        public boolean isComplete(PublishProductToChannelContext context) {
            return context.productAsserted();
        }
    }

    private static final class AssertChannelStockPathStep implements StepDefinition<PublishProductToChannelContext> {
        @Override
        public String name() {
            return PublishProductToChannelWorkflowNames.STEP_ASSERT_CHANNEL_STOCK_PATH;
        }

        @Override
        public List<Event> onEnter(String workflowId, PublishProductToChannelContext context) {
            return List.of(new RequestCheckChannelStockPathEvent(
                    workflowId,
                    context.merchantId(),
                    context.salesChannelId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public PublishProductToChannelContext onSignal(PublishProductToChannelContext context, InboundSignal signal) {
            if (signal.event() instanceof StockPathCheckedEvent event) {
                return context.withStockPathChecked(event.missingRoute());
            }
            return context;
        }

        @Override
        public boolean isComplete(PublishProductToChannelContext context) {
            return context.stockPathChecked();
        }

        @Override
        public Object checkpointOutput(PublishProductToChannelContext context) {
            return context.missingRoute();
        }
    }

    private static final class WritePublicationStep implements StepDefinition<PublishProductToChannelContext> {
        @Override
        public String name() {
            return PublishProductToChannelWorkflowNames.STEP_WRITE_PUBLICATION;
        }

        @Override
        public List<Event> onEnter(String workflowId, PublishProductToChannelContext context) {
            return List.of(new RequestWritePublicationEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    context.salesChannelId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public PublishProductToChannelContext onSignal(PublishProductToChannelContext context, InboundSignal signal) {
            if (signal.event() instanceof ProductPublishedToChannelEvent) {
                return context.withPublicationWritten();
            }
            return context;
        }

        @Override
        public boolean isComplete(PublishProductToChannelContext context) {
            return context.publicationWritten();
        }

        @Override
        public List<Event> compensate(String workflowId, PublishProductToChannelContext context) {
            if (!context.publicationWritten() || context.publicationCompensated()) {
                return List.of();
            }
            return List.of(new RequestUnpublishProductCompensationEvent(
                    workflowId,
                    context.merchantId(),
                    context.productId(),
                    context.salesChannelId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }

        @Override
        public PublishProductToChannelContext onCompensationAck(
                PublishProductToChannelContext context,
                InboundSignal signal
        ) {
            if (signal.event() instanceof ProductUnpublishedFromChannelEvent) {
                return context.withPublicationCompensated();
            }
            return context;
        }

        @Override
        public boolean isCompensated(PublishProductToChannelContext context) {
            return !context.publicationWritten() || context.publicationCompensated();
        }
    }
}
