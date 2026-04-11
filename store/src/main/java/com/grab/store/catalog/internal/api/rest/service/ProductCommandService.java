package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateProductStatusModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.*;
import com.grab.store.catalog.internal.api.rest.dto.response.*;
import com.grab.store.catalog.internal.api.rest.mapper.SaveProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductStatusDtoMapper;
import com.grab.store.catalog.internal.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private static final Logger log = Loggers.getLogger(ProductCommandService.class);

    private final CommandBus commandBus;
    private final SaveProductDtoMapper saveProductDtoMapper;
    private final DeleteProductModelAssembler deleteProductModelAssembler;
    private final UpdateProductDtoMapper updateProductDtoMapper;
    private final UpdateProductStatusDtoMapper updateProductStatusDtoMapper;
    private final UpdateProductModelAssembler updateProductModelAssembler;
    private final UpdateProductStatusModelAssembler updateProductStatusModelAssembler;
    private final IdGenerator idGenerator;

    public String saveProduct(SaveProductRequest request) {
        log.info("Saving product: {}", request.product().name());

        CreateProductSetCommand command = saveProductDtoMapper.toCommand(request);
        CreateProductSetResult result = commandBus.dispatch(command);

        return result.productId();
    }

    public EntityModel<DeleteProductResponse> deleteProduct(String productId) {
        log.info("Deleting product: {}", productId);

        DeleteProductCommand command = new DeleteProductCommand(idGenerator.convertIdFrom(productId));
        DeleteProductResult result = commandBus.dispatch(command);
        DeleteProductResponse response = new DeleteProductResponse(productId, result.deleted());

        return deleteProductModelAssembler.toModel(response);
    }

    public EntityModel<UpdateProductResponse> updateProduct(String productId, UpdateProductRequest request) {
        log.info("Updating product: {}", productId);

        UpdateProductCommand command = updateProductDtoMapper.toCommand(productId, request);
        UpdateProductResult result = commandBus.dispatch(command);
        UpdateProductResponse response = updateProductDtoMapper.toResponse(result);

        return updateProductModelAssembler.toModel(response);
    }

    public EntityModel<ProductDescriptionsResponse> replaceProductDescriptions(String productId, ReplaceProductDescriptionsRequest request) {
        ReplaceProductDescriptionsCommand command = new ReplaceProductDescriptionsCommand(
                idGenerator.convertIdFrom(productId),
                request.descriptions().stream()
                        .map(description -> new ReplaceProductDescriptionsCommand.Description(
                                description.id() == null || description.id().isBlank() ? null : idGenerator.convertIdFrom(description.id()),
                                description.name(),
                                description.title(),
                                description.description()
                        ))
                        .toList()
        );
        ProductDescriptionsResult result = commandBus.dispatch(command);
        return EntityModel.of(new ProductDescriptionsResponse(result.productId(), mapDescriptions(result)));
    }

    public EntityModel<ProductMediaResponse> replaceProductMedia(String productId, ReplaceProductMediaRequest request) {
        ReplaceProductMediaCommand command = new ReplaceProductMediaCommand(
                idGenerator.convertIdFrom(productId),
                request.medias().stream()
                        .map(media -> new ReplaceProductMediaCommand.Media(
                                media.id() == null || media.id().isBlank() ? null : idGenerator.convertIdFrom(media.id()),
                                media.type(),
                                media.path()
                        ))
                        .toList()
        );
        ProductMediaResult result = commandBus.dispatch(command);
        return EntityModel.of(new ProductMediaResponse(result.productId(), mapMedias(result)));
    }

    public EntityModel<UpdateProductStatusResponse> updateProductStatus(String productId, UpdateProductStatusRequest request) {
        log.info("Updating product status: {}", productId);

        UpdateProductStatusCommand command = updateProductStatusDtoMapper.toCommand(productId, request);
        UpdateProductStatusResult result = commandBus.dispatch(command);
        UpdateProductStatusResponse response = updateProductStatusDtoMapper.toResponse(result);

        return updateProductStatusModelAssembler.toModel(response);
    }

    public EntityModel<ProductModerationResponse> moderateProduct(String productId, String action, ProductModerationRequest request) {
        ModerateProductCommand command = new ModerateProductCommand(
                idGenerator.convertIdFrom(productId),
                action,
                request == null ? null : request.reason()
        );
        ModerateProductResult result = commandBus.dispatch(command);
        return EntityModel.of(new ProductModerationResponse(
                result.productId(),
                result.action(),
                result.oldStatus(),
                result.newStatus(),
                result.reason()
        ));
    }

    private java.util.List<GetProductResponse.Description> mapDescriptions(ProductDescriptionsResult result) {
        return result.descriptions().stream()
                .map(description -> new GetProductResponse.Description(
                        description.id() == null ? null : description.id().getValue(),
                        description.name(),
                        description.title(),
                        description.description()
                ))
                .toList();
    }

    private java.util.List<GetProductResponse.Media> mapMedias(ProductMediaResult result) {
        return result.medias().stream()
                .map(media -> new GetProductResponse.Media(
                        media.id() == null ? null : media.id().getValue(),
                        media.type(),
                        media.path()
                ))
                .toList();
    }
}
