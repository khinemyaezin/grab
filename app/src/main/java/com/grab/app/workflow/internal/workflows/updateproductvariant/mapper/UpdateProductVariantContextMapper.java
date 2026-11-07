package com.grab.app.workflow.internal.workflows.updateproductvariant.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.command.model.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.workflow.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.app.workflow.port.event.InventorySyncPayload;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult.PricePair;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateProductVariantContextMapper {

    public UpdateProductVariantContext toContext(
            StartUpdateProductVariantWorkflowCommand command,
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId
    ) {
        UpdateProductVariantMessage message = command.message();
        return UpdateProductVariantContext.createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                message.productId() != null ? message.productId().getValue() : null,
                message.variantId() != null ? message.variantId().getValue() : null,
                message.sku(),
                message.manageInventory(),
                toContextPrice(message.price()),
                toContextInventoryLines(message.inventoryLines())
        );
    }

    public UpdateProductVariantResult toResult(WorkflowInstance instance, UpdateProductVariantContext context) {
        if (instance == null) {
            return null;
        }
        PricePair pricePair = null;
        if (context != null && context.pricePair() != null) {
            pricePair = new PricePair(
                    context.pricePair().variantId(),
                    context.pricePair().sku(),
                    context.pricePair().priceSetId()
            );
        }
        boolean terminalPartial = instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATED;
        boolean partiallyApplied = terminalPartial && context != null && context.isPartiallyApplied();

        List<Id> inventoryItemIds = context == null
                ? List.of()
                : context.inventoryItemIds().stream().<Id>map(CommonId::new).toList();

        return new UpdateProductVariantResult(
                new CommonId(instance.id()),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null || context.productId() == null ? null : new CommonId(context.productId()),
                context == null || context.variantId() == null ? null : new CommonId(context.variantId()),
                context == null ? null : context.skuForPrice(),
                context != null && context.variantUpdated(),
                pricePair,
                inventoryItemIds,
                context == null ? 0 : context.compensatedPriceSetIds().size(),
                partiallyApplied,
                instance.errorMessage().orElse(null)
        );
    }

    protected UpdateProductVariantContext.Price toContextPrice(UpdateProductVariantMessage.Price price) {
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

    protected List<UpdateProductVariantContext.InventoryLine> toContextInventoryLines(
            List<UpdateProductVariantMessage.InventoryLine> lines
    ) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(this::toContextInventoryLine).toList();
    }

    protected abstract UpdateProductVariantContext.InventoryLine toContextInventoryLine(
            UpdateProductVariantMessage.InventoryLine line
    );

    protected abstract InventorySyncPayload.CreateStock toCreateStock(UpdateProductVariantMessage.CreateStock create);

    protected abstract InventorySyncPayload.AdjustStock toAdjustStock(UpdateProductVariantMessage.AdjustStock adjust);

    protected abstract InventorySyncPayload.DamageStock toDamageStock(UpdateProductVariantMessage.DamageStock damage);

    protected abstract InventorySyncPayload.WriteOffStock toWriteOffStock(UpdateProductVariantMessage.WriteOffStock writeOff);

    protected abstract InventorySyncPayload.Reorder toReorder(UpdateProductVariantMessage.Reorder reorder);
}
