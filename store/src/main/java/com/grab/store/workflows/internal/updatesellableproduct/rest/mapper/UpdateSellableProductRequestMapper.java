package com.grab.store.workflows.internal.updatesellableproduct.rest.mapper;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.store.workflows.internal.updatesellableproduct.UpdateSellableProductContext;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request.UpdateSellableProductRequest;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.response.UpdateSellableProductResponse;
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
        return new UpdateSellableProductResponse(
                instance.id(),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null ? null : context.productId(),
                pricePairs,
                context == null ? List.of() : context.inventoryItemIds(),
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
}
