package com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.mapper;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.store.workflows.events.InventorySyncPayload;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.dto.request.UpdateSellableProductRequest;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.dto.response.UpdateSellableProductResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class UpdateSellableProductRequestMapper {

    public UpdateSellableProductContext toContext(
            UpdateSellableProductRequest request,
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId
    ) {
        UpdateSellableProductContext.Product product = toContextProduct(request.product());
        List<UpdateSellableProductContext.InventoryLine> inventoryLines = request.inventoryLines() == null
                ? List.of()
                : request.inventoryLines().stream().map(this::toContextInventoryLine).toList();
        List<UpdateSellableProductContext.PricingLine> pricingLines = request.pricingLines() == null
                ? List.of()
                : request.pricingLines().stream().map(this::toContextPricingLine).toList();
        return UpdateSellableProductContext.createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                request.productId(),
                product,
                inventoryLines,
                pricingLines
        );
    }

    public UpdateSellableProductResponse toResponse(WorkflowInstance instance, UpdateSellableProductContext context) {
        List<UpdateSellableProductResponse.PricePair> pricePairs = context == null
                ? List.of()
                : context.pricePairs().stream()
                .map(pair -> new UpdateSellableProductResponse.PricePair(
                        pair.variantId(),
                        pair.sku(),
                        pair.priceSetId()
                ))
                .toList();
        boolean terminalPartial = instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATED;
        boolean partiallyApplied = terminalPartial && context != null && context.isPartiallyApplied();
        return new UpdateSellableProductResponse(
                instance.id(),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null ? null : context.productId(),
                context != null && context.productUpdated(),
                pricePairs,
                context == null ? List.of() : context.inventoryItemIds(),
                context == null ? 0 : context.compensatedPriceSetIds().size(),
                partiallyApplied,
                instance.errorMessage().orElse(null)
        );
    }

    protected abstract UpdateSellableProductContext.Product toContextProduct(UpdateSellableProductRequest.Product product);

    protected abstract UpdateSellableProductContext.InventoryLine toContextInventoryLine(
            UpdateSellableProductRequest.InventoryLine inventoryLine
    );

    protected abstract UpdateSellableProductContext.PricingLine toContextPricingLine(
            UpdateSellableProductRequest.PricingLine pricingLine
    );

    protected abstract InventorySyncPayload.CreateStock toCreateStock(UpdateSellableProductRequest.CreateStock create);

    protected abstract InventorySyncPayload.AdjustStock toAdjustStock(UpdateSellableProductRequest.AdjustStock adjust);

    protected abstract InventorySyncPayload.DamageStock toDamageStock(UpdateSellableProductRequest.DamageStock damage);

    protected abstract InventorySyncPayload.WriteOffStock toWriteOffStock(
            UpdateSellableProductRequest.WriteOffStock writeOff
    );

    protected abstract InventorySyncPayload.Reorder toReorder(UpdateSellableProductRequest.Reorder reorder);
}
