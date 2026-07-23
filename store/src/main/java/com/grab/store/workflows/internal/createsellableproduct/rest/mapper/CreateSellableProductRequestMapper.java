package com.grab.store.workflows.internal.createsellableproduct.rest.mapper;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.store.workflows.internal.createsellableproduct.CreateSellableProductContext;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.request.CreateSellableProductRequest;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.response.CreateSellableProductResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellableProductRequestMapper {

    public CreateSellableProductContext toContext(
            CreateSellableProductRequest request,
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId
    ) {
        CreateSellableProductContext.Product product = toContextProduct(request.product());
        List<CreateSellableProductContext.VariantType> variantTypes = request.variantTypes() == null
                ? List.of()
                : request.variantTypes().stream().map(this::toContextVariantType).toList();
        List<CreateSellableProductContext.InventoryLine> inventoryLines = request.inventoryLines().stream()
                .map(this::toContextInventoryLine)
                .toList();
        return CreateSellableProductContext.createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines
        );
    }

    public CreateSellableProductResponse toResponse(WorkflowInstance instance, CreateSellableProductContext context) {
        return new CreateSellableProductResponse(
                instance.id(),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null ? null : context.productId(),
                context == null ? List.of() : context.inventoryItemIds(),
                instance.errorMessage().orElse(null)
        );
    }

    protected abstract CreateSellableProductContext.Product toContextProduct(CreateSellableProductRequest.Product product);

    protected abstract CreateSellableProductContext.VariantType toContextVariantType(CreateSellableProductRequest.VariantType variantType);

    protected abstract CreateSellableProductContext.InventoryLine toContextInventoryLine(CreateSellableProductRequest.InventoryLine inventoryLine);
}
