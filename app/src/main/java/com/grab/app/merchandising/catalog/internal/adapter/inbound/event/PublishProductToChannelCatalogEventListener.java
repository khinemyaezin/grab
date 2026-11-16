package com.grab.app.merchandising.catalog.internal.adapter.inbound.event;

import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.catalog.application.model.read.CheckProductPublishableQuery;
import com.catalog.application.model.read.CheckProductPublishableResult;
import com.catalog.application.model.write.PublishProductToChannelCommand;
import com.catalog.application.model.write.UnpublishProductFromChannelCommand;
import com.grab.app.workflow.port.event.ProductAssertedEvent;
import com.grab.app.workflow.port.event.ProductPublishedToChannelEvent;
import com.grab.app.workflow.port.event.ProductUnpublishedFromChannelEvent;
import com.grab.app.workflow.port.event.PublishProductStepFailedEvent;
import com.grab.app.workflow.port.event.RequestAssertProductEvent;
import com.grab.app.workflow.port.event.RequestUnpublishProductCompensationEvent;
import com.grab.app.workflow.port.event.RequestUnpublishProductFromChannelEvent;
import com.grab.app.workflow.port.event.RequestWritePublicationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.domain.Event;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PublishProductToChannelCatalogEventListener {

    private static final Logger log = Loggers.getLogger(PublishProductToChannelCatalogEventListener.class);
    private static final int EVENT_VERSION = 1;

    private final QueryBus queryBus;
    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final CatalogWorkflowStepRunner signalEmitter;

    public PublishProductToChannelCatalogEventListener(
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            IdGenerator idGenerator,
            CatalogWorkflowStepRunner signalEmitter
    ) {
        this.queryBus = queryBus;
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
        this.signalEmitter = signalEmitter;
    }

    @EventListener
    public void onRequestAssertProduct(RequestAssertProductEvent event) {
        log.info(
                "Handling RequestAssertProductEvent workflowId={} productId={}",
                event.workflowId(),
                event.productId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> assertProduct(event),
                exception -> {
                    log.warn(
                            "Assert product failed for workflowId={}: {}",
                            event.workflowId(),
                            exception.getMessage()
                    );
                    return List.of(new PublishProductStepFailedEvent(
                            event.workflowId(),
                            "assert-product",
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    @EventListener
    public void onRequestWritePublication(RequestWritePublicationEvent event) {
        log.info(
                "Handling RequestWritePublicationEvent workflowId={} productId={} variantId={} salesChannelId={}",
                event.workflowId(),
                event.productId(),
                event.variantId(),
                event.salesChannelId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> writePublication(event),
                exception -> {
                    log.warn(
                            "Write publication failed for workflowId={}: {}",
                            event.workflowId(),
                            exception.getMessage()
                    );
                    return List.of(new PublishProductStepFailedEvent(
                            event.workflowId(),
                            "write-publication",
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    @EventListener
    public void onRequestUnpublishProductFromChannel(RequestUnpublishProductFromChannelEvent event) {
        log.info(
                "Handling RequestUnpublishProductFromChannelEvent workflowId={} productId={} variantId={} salesChannelId={}",
                event.workflowId(),
                event.productId(),
                event.variantId(),
                event.salesChannelId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> unpublish(
                        event.workflowId(),
                        event.merchantId(),
                        event.productId(),
                        event.variantId(),
                        event.salesChannelId()
                ),
                exception -> {
                    log.warn(
                            "Unpublish product failed for workflowId={}: {}",
                            event.workflowId(),
                            exception.getMessage()
                    );
                    return List.of(new PublishProductStepFailedEvent(
                            event.workflowId(),
                            "unpublish-publication",
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    @EventListener
    public void onRequestUnpublishProductCompensation(RequestUnpublishProductCompensationEvent event) {
        log.info(
                "Compensating unpublish workflowId={} productId={} variantId={} salesChannelId={}",
                event.workflowId(),
                event.productId(),
                event.variantId(),
                event.salesChannelId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> unpublish(
                        event.workflowId(),
                        event.merchantId(),
                        event.productId(),
                        event.variantId(),
                        event.salesChannelId()
                ),
                exception -> {
                    log.warn(
                            "Compensation unpublish failed workflowId={} productId={}: {}",
                            event.workflowId(),
                            event.productId(),
                            exception.getMessage()
                    );
                    return List.of();
                }
        );
    }

    private List<Event> assertProduct(RequestAssertProductEvent event) {
        CheckProductPublishableResult result = queryBus.dispatch(new CheckProductPublishableQuery(
                event.productId(),
                event.merchantId()
        ));
        if (!result.found() || !result.owned()) {
            throw new IllegalStateException(failureMessage(result));
        }
        return List.of(new ProductAssertedEvent(
                event.workflowId(),
                event.productId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private List<Event> writePublication(RequestWritePublicationEvent event) {
        commandBus.dispatch(new PublishProductToChannelCommand(
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.productId()),
                idGenerator.convertIdFrom(event.variantId()),
                idGenerator.convertIdFrom(event.salesChannelId())
        ));
        return List.of(new ProductPublishedToChannelEvent(
                event.workflowId(),
                event.productId(),
                event.variantId(),
                event.salesChannelId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private List<Event> unpublish(
            String workflowId,
            String merchantId,
            String productId,
            String variantId,
            String salesChannelId
    ) {
        commandBus.dispatch(new UnpublishProductFromChannelCommand(
                idGenerator.convertIdFrom(merchantId),
                idGenerator.convertIdFrom(productId),
                idGenerator.convertIdFrom(variantId),
                idGenerator.convertIdFrom(salesChannelId)
        ));
        return List.of(new ProductUnpublishedFromChannelEvent(
                workflowId,
                productId,
                variantId,
                salesChannelId,
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private String failureMessage(CheckProductPublishableResult result) {
        if (!result.found()) {
            return "Product not found";
        }
        return "Merchant does not own this product";
    }
}

