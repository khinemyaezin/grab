package com.grab.store.catalog.internal.event;

import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.command.ApplyProductStatusCommand;
import com.catalog.application.command.ApplyProductStatusResult;
import com.catalog.application.command.ReplaceProductDescriptionsCommand;
import com.catalog.application.command.ReplaceProductMediaCommand;
import com.grab.store.workflows.events.ProductDescriptionsReplacedEvent;
import com.grab.store.workflows.events.ProductMediaReplacedEvent;
import com.grab.store.workflows.events.ProductStatusAppliedEvent;
import com.grab.store.workflows.events.RequestApplyProductStatusEvent;
import com.grab.store.workflows.events.RequestReplaceProductDescriptionsEvent;
import com.grab.store.workflows.events.RequestReplaceProductMediaEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SellableProductListingCatalogEventListener {

    private static final Logger log = Loggers.getLogger(SellableProductListingCatalogEventListener.class);
    private static final int EVENT_VERSION = 1;

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final CatalogWorkflowStepRunner signalEmitter;

    @EventListener
    public void onRequestReplaceProductMedia(RequestReplaceProductMediaEvent event) {
        log.info(
                "Handling RequestReplaceProductMediaEvent workflowId={} productId={}",
                event.workflowId(),
                event.productId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> replaceMedia(event),
                exception -> failed(event.workflowId(), "replace-medias", exception)
        );
    }

    @EventListener
    public void onRequestReplaceProductDescriptions(RequestReplaceProductDescriptionsEvent event) {
        log.info(
                "Handling RequestReplaceProductDescriptionsEvent workflowId={} productId={}",
                event.workflowId(),
                event.productId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> replaceDescriptions(event),
                exception -> failed(event.workflowId(), "replace-descriptions", exception)
        );
    }

    @EventListener
    public void onRequestApplyProductStatus(RequestApplyProductStatusEvent event) {
        log.info(
                "Handling RequestApplyProductStatusEvent workflowId={} productId={} status={}",
                event.workflowId(),
                event.productId(),
                event.status()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> applyStatus(event),
                exception -> {
                    log.warn("Apply status failed for workflowId={}, skipping and continuing: {}",
                            event.workflowId(), exception.getMessage());
                    return List.of(new ProductStatusAppliedEvent(
                            event.workflowId(),
                            event.productId(),
                            event.status(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    private List<Event> replaceMedia(RequestReplaceProductMediaEvent event) {
        List<ReplaceProductMediaCommand.Media> medias = event.medias().stream()
                .map(media -> new ReplaceProductMediaCommand.Media(
                        media.id() == null || media.id().isBlank() ? null : idGenerator.convertIdFrom(media.id()),
                        media.storageKey(),
                        media.contentType(),
                        media.rank()
                ))
                .toList();
        commandBus.dispatch(new ReplaceProductMediaCommand(
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.productId()),
                medias
        ));
        return List.of(new ProductMediaReplacedEvent(
                event.workflowId(),
                event.productId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private List<Event> replaceDescriptions(RequestReplaceProductDescriptionsEvent event) {
        List<ReplaceProductDescriptionsCommand.Description> descriptions = event.descriptions().stream()
                .map(description -> new ReplaceProductDescriptionsCommand.Description(
                        description.id() == null || description.id().isBlank()
                                ? null
                                : idGenerator.convertIdFrom(description.id()),
                        description.name(),
                        description.title(),
                        description.description()
                ))
                .toList();
        commandBus.dispatch(new ReplaceProductDescriptionsCommand(
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.productId()),
                descriptions
        ));
        return List.of(new ProductDescriptionsReplacedEvent(
                event.workflowId(),
                event.productId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private List<Event> applyStatus(RequestApplyProductStatusEvent event) {
        ApplyProductStatusResult result = commandBus.dispatch(new ApplyProductStatusCommand(
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.productId()),
                event.status()
        ));
        return List.of(new ProductStatusAppliedEvent(
                event.workflowId(),
                result.productId(),
                result.status(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private List<Event> failed(String workflowId, String step, Exception exception) {
        log.warn(
                "{} failed for workflowId={}: {}",
                step,
                workflowId,
                exception.getMessage()
        );
        return List.of(new SellableProductStepFailedEvent(
                workflowId,
                step,
                exception.getMessage(),
                Instant.now(),
                EVENT_VERSION
        ));
    }
}
