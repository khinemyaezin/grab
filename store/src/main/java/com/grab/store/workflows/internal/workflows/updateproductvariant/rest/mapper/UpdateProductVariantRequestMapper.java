package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.mapper;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.request.UpdateProductVariantRequest;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.response.UpdateProductVariantResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateProductVariantRequestMapper {

    public UpdateProductVariantContext toContext(
            UpdateProductVariantRequest request,
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId
    ) {
        return UpdateProductVariantContext.createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                request.productId(),
                request.variantId(),
                request.sku(),
                toContextPrice(request.price()),
                toContextAdjustStock(request.adjustStock())
        );
    }

    public UpdateProductVariantResponse toResponse(WorkflowInstance instance, UpdateProductVariantContext context) {
        UpdateProductVariantResponse.PricePair pricePair = null;
        if (context != null && context.pricePair() != null) {
            pricePair = new UpdateProductVariantResponse.PricePair(
                    context.pricePair().variantId(),
                    context.pricePair().sku(),
                    context.pricePair().priceSetId()
            );
        }
        return new UpdateProductVariantResponse(
                instance.id(),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null ? null : context.productId(),
                context == null ? null : context.variantId(),
                context == null ? null : context.skuForPrice(),
                pricePair,
                context == null ? null : context.inventoryItemId(),
                instance.errorMessage().orElse(null)
        );
    }

    private UpdateProductVariantContext.Price toContextPrice(UpdateProductVariantRequest.Price price) {
        if (price == null) {
            return null;
        }
        List<UpdateProductVariantContext.PriceRule> rules = price.rules() == null
                ? List.of()
                : price.rules().stream()
                .map(rule -> new UpdateProductVariantContext.PriceRule(
                        rule.attribute(),
                        rule.value(),
                        rule.operator(),
                        rule.priority()
                ))
                .toList();
        return new UpdateProductVariantContext.Price(
                price.title(),
                price.currencyCode(),
                price.amount(),
                price.minQuantity(),
                price.maxQuantity(),
                rules
        );
    }

    private UpdateProductVariantContext.AdjustStock toContextAdjustStock(
            UpdateProductVariantRequest.AdjustStock adjustStock
    ) {
        if (adjustStock == null) {
            return null;
        }
        return new UpdateProductVariantContext.AdjustStock(
                adjustStock.inventoryItemId(),
                adjustStock.newOnHandQuantity(),
                adjustStock.reason()
        );
    }
}
