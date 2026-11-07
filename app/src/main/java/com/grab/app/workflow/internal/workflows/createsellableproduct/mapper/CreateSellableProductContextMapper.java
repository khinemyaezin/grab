package com.grab.app.workflow.internal.workflows.createsellableproduct.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.command.model.StartCreateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.workflows.createsellableproduct.CreateSellableProductContext;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult.PricePair;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.workflow.WorkflowInstance;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateSellableProductContextMapper {

    public CreateSellableProductContext toContext(
            StartCreateSellableProductWorkflowCommand command,
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId
    ) {
        CreateSellableProductMessage message = command.message();
        CreateSellableProductContext.Product product = toContextProduct(message.product());
        List<CreateSellableProductContext.VariantType> variantTypes = message.variantTypes() == null
                ? List.of()
                : message.variantTypes().stream().map(this::toContextVariantType).toList();
        List<CreateSellableProductContext.InventoryLine> inventoryLines = message.inventoryLines() == null
                ? List.of()
                : message.inventoryLines().stream().map(this::toContextInventoryLine).toList();
        List<CreateSellableProductContext.PricingLine> pricingLines = message.pricingLines() == null
                ? List.of()
                : message.pricingLines().stream().map(this::toContextPricingLine).toList();
        List<CreateSellableProductContext.PublicationLine> publicationLines = message.publicationLines() == null
                ? List.of()
                : message.publicationLines().stream().map(this::toContextPublicationLine).toList();
        List<CreateSellableProductContext.MediaLine> medias = message.medias() == null
                ? null
                : message.medias().stream().map(this::toContextMedia).toList();
        List<CreateSellableProductContext.DescriptionLine> descriptions = message.descriptions() == null
                ? null
                : message.descriptions().stream().map(this::toContextDescription).toList();

        return CreateSellableProductContext.createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                publicationLines,
                medias,
                descriptions
        );
    }

    public CreateSellableProductResult toResult(WorkflowInstance instance, CreateSellableProductContext context) {
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

        List<Id> inventoryItemIds = context == null
                ? List.of()
                : context.inventoryItemIds().stream().<Id>map(CommonId::new).toList();

        return new CreateSellableProductResult(
                new CommonId(instance.id()),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null || context.productId() == null ? null : new CommonId(context.productId()),
                pricePairs,
                inventoryItemIds,
                instance.errorMessage().orElse(null),
                instance.updatedAt()
        );
    }

    protected abstract CreateSellableProductContext.Product toContextProduct(CreateSellableProductMessage.Product product);

    protected abstract CreateSellableProductContext.VariantType toContextVariantType(CreateSellableProductMessage.VariantType variantType);

    protected abstract CreateSellableProductContext.InventoryLine toContextInventoryLine(CreateSellableProductMessage.InventoryLine inventoryLine);

    protected abstract CreateSellableProductContext.PricingLine toContextPricingLine(CreateSellableProductMessage.PricingLine pricingLine);

    protected abstract CreateSellableProductContext.PublicationLine toContextPublicationLine(CreateSellableProductMessage.PublicationLine publicationLine);

    protected abstract CreateSellableProductContext.MediaLine toContextMedia(CreateSellableProductMessage.MediaLine media);

    protected abstract CreateSellableProductContext.DescriptionLine toContextDescription(CreateSellableProductMessage.DescriptionLine description);
}
