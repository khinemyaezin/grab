package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.mapper;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.store.workflows.events.InventorySyncPayload;
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
                toContextInventoryLines(request.inventoryLines())
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
        boolean terminalPartial = instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATED;
        boolean partiallyApplied = terminalPartial && context != null && context.isPartiallyApplied();
        return new UpdateProductVariantResponse(
                instance.id(),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null ? null : context.productId(),
                context == null ? null : context.variantId(),
                context == null ? null : context.skuForPrice(),
                context != null && context.variantUpdated(),
                pricePair,
                context == null ? List.of() : context.inventoryItemIds(),
                context == null ? 0 : context.compensatedPriceSetIds().size(),
                partiallyApplied,
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

    private List<UpdateProductVariantContext.InventoryLine> toContextInventoryLines(
            List<UpdateProductVariantRequest.InventoryLine> lines
    ) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(this::toContextInventoryLine).toList();
    }

    private UpdateProductVariantContext.InventoryLine toContextInventoryLine(
            UpdateProductVariantRequest.InventoryLine line
    ) {
        return new UpdateProductVariantContext.InventoryLine(
                line.sku(),
                line.locationId(),
                line.inventoryItemId(),
                line.op(),
                toCreateStock(line.create()),
                toAdjustStock(line.adjust()),
                toDamageStock(line.damage()),
                toWriteOffStock(line.writeOff()),
                toReorder(line.reorder())
        );
    }

    private InventorySyncPayload.CreateStock toCreateStock(UpdateProductVariantRequest.CreateStock create) {
        if (create == null) {
            return null;
        }
        return new InventorySyncPayload.CreateStock(
                create.initialQuantity(),
                create.safetyStock(),
                create.reorderPoint(),
                create.reorderQuantity(),
                create.maxStock()
        );
    }

    private InventorySyncPayload.AdjustStock toAdjustStock(UpdateProductVariantRequest.AdjustStock adjust) {
        if (adjust == null) {
            return null;
        }
        return new InventorySyncPayload.AdjustStock(adjust.newOnHandQuantity(), adjust.reason());
    }

    private InventorySyncPayload.DamageStock toDamageStock(UpdateProductVariantRequest.DamageStock damage) {
        if (damage == null) {
            return null;
        }
        return new InventorySyncPayload.DamageStock(damage.quantity(), damage.notes());
    }

    private InventorySyncPayload.WriteOffStock toWriteOffStock(UpdateProductVariantRequest.WriteOffStock writeOff) {
        if (writeOff == null) {
            return null;
        }
        return new InventorySyncPayload.WriteOffStock(writeOff.quantity(), writeOff.reason(), writeOff.notes());
    }

    private InventorySyncPayload.Reorder toReorder(UpdateProductVariantRequest.Reorder reorder) {
        if (reorder == null) {
            return null;
        }
        return new InventorySyncPayload.Reorder(
                reorder.safetyStock(),
                reorder.reorderPoint(),
                reorder.reorderQuantity(),
                reorder.maxStock()
        );
    }
}
