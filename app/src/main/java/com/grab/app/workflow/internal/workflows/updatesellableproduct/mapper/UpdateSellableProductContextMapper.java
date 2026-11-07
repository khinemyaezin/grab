package com.grab.app.workflow.internal.workflows.updatesellableproduct.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.command.model.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.app.workflow.port.event.InventorySyncPayload;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult.PricePair;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult.PublicationPair;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateSellableProductContextMapper {

    public UpdateSellableProductContext toContext(
            StartUpdateSellableProductWorkflowCommand command,
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId
    ) {
        UpdateSellableProductMessage message = command.message();
        UpdateSellableProductContext.Product product = toContextProduct(message.product());
        List<UpdateSellableProductContext.InventoryLine> inventoryLines = message.inventoryLines() == null
                ? List.of()
                : message.inventoryLines().stream().map(this::toContextInventoryLine).toList();
        List<UpdateSellableProductContext.PricingLine> pricingLines = message.pricingLines() == null
                ? List.of()
                : message.pricingLines().stream().map(this::toContextPricingLine).toList();
        List<UpdateSellableProductContext.PublicationLine> publicationLines = message.publicationLines() == null
                ? List.of()
                : message.publicationLines().stream().map(this::toContextPublicationLine).toList();
        List<UpdateSellableProductContext.PublicationLine> unpublishLines = message.unpublishLines() == null
                ? List.of()
                : message.unpublishLines().stream().map(this::toContextPublicationLine).toList();
        List<UpdateSellableProductContext.MediaLine> medias = message.medias() == null
                ? null
                : message.medias().stream().map(this::toContextMedia).toList();
        List<UpdateSellableProductContext.DescriptionLine> descriptions = message.descriptions() == null
                ? null
                : message.descriptions().stream().map(this::toContextDescription).toList();

        return UpdateSellableProductContext.createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                message.productId() != null ? message.productId().getValue() : null,
                product,
                inventoryLines,
                pricingLines,
                publicationLines,
                unpublishLines,
                medias,
                descriptions
        );
    }

    public UpdateSellableProductResult toResult(WorkflowInstance instance, UpdateSellableProductContext context) {
        if (instance == null) {
            return null;
        }
        List<PricePair> pricePairs = context == null
                ? List.of()
                : context.pricePairs().stream()
                .map(pair -> new PricePair(
                        pair.variantId(),
                        pair.sku(),
                        pair.priceSetId()
                ))
                .toList();

        List<PublicationPair> writtenPublications = context == null
                ? List.of()
                : context.writtenPublications().stream()
                .map(pair -> new PublicationPair(
                        pair.variantId(),
                        pair.sku(),
                        pair.salesChannelId()
                ))
                .toList();

        List<String> missingRouteChannelIds = context == null
                ? List.of()
                : List.copyOf(context.missingRouteChannelIds());
        List<Id> inventoryItemIds = context == null
                ? List.of()
                : context.inventoryItemIds().stream().<Id>map(CommonId::new).toList();
        int compensatedPriceSetCount = context == null ? 0 : context.compensatedPriceSetIds().size();
        CommonId productId = context == null || context.productId() == null ? null : new CommonId(context.productId());
        boolean productUpdated = context != null && context.productUpdated();
        boolean terminalPartial = instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATED;
        boolean partiallyApplied = terminalPartial && context != null && context.isPartiallyApplied();

        return new UpdateSellableProductResult(
                new CommonId(instance.id()),
                instance.status().name(),
                instance.currentStep().orElse(null),
                productId,
                productUpdated,
                pricePairs,
                inventoryItemIds,
                compensatedPriceSetCount,
                writtenPublications,
                missingRouteChannelIds,
                partiallyApplied,
                instance.errorMessage().orElse(null)
        );
    }

    protected abstract UpdateSellableProductContext.Product toContextProduct(UpdateSellableProductMessage.Product product);

    protected abstract UpdateSellableProductContext.InventoryLine toContextInventoryLine(
            UpdateSellableProductMessage.InventoryLine inventoryLine
    );

    protected abstract UpdateSellableProductContext.PricingLine toContextPricingLine(
            UpdateSellableProductMessage.PricingLine pricingLine
    );

    protected abstract UpdateSellableProductContext.PublicationLine toContextPublicationLine(
            UpdateSellableProductMessage.PublicationLine publicationLine
    );

    protected abstract UpdateSellableProductContext.MediaLine toContextMedia(UpdateSellableProductMessage.MediaLine media);

    protected abstract UpdateSellableProductContext.DescriptionLine toContextDescription(
            UpdateSellableProductMessage.DescriptionLine description
    );

    protected abstract InventorySyncPayload.CreateStock toCreateStock(UpdateSellableProductMessage.CreateStock create);

    protected abstract InventorySyncPayload.AdjustStock toAdjustStock(UpdateSellableProductMessage.AdjustStock adjust);

    protected abstract InventorySyncPayload.DamageStock toDamageStock(UpdateSellableProductMessage.DamageStock damage);

    protected abstract InventorySyncPayload.WriteOffStock toWriteOffStock(UpdateSellableProductMessage.WriteOffStock writeOff);

    protected abstract InventorySyncPayload.Reorder toReorder(UpdateSellableProductMessage.Reorder reorder);
}
