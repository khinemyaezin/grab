package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.dto.request.*;
import com.grab.store.catalog.internal.api.rest.dto.response.*;
import com.grab.store.catalog.internal.api.rest.mapper.SaveProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductStatusDtoMapper;
import com.grab.store.catalog.internal.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private static final Logger log = Loggers.getLogger(ProductCommandService.class);

    private final CommandBus commandBus;
    private final SaveProductDtoMapper saveProductDtoMapper;
    private final UpdateProductDtoMapper updateProductDtoMapper;
    private final UpdateProductStatusDtoMapper updateProductStatusDtoMapper;
    private final IdGenerator idGenerator;

    public String saveProduct(SaveProductRequest request) {
        log.info("Saving product: {}", request.product().name());

        CreateProductSetCommand command = saveProductDtoMapper.toCommand(request);
        CreateProductSetResult result = commandBus.dispatch(command);

        return result.productId();
    }

    public DeleteProductResponse deleteProduct(String productId) {
        log.info("Deleting product: {}", productId);

        DeleteProductCommand command = new DeleteProductCommand(idGenerator.convertIdFrom(productId));
        DeleteProductResult result = commandBus.dispatch(command);
        
        return new DeleteProductResponse(productId, result.deleted());
    }

    public UpdateProductResponse updateProduct(String productId, UpdateProductRequest request) {
        log.info("Updating product: {}", productId);

        UpdateProductCommand command = updateProductDtoMapper.toCommand(productId, request);
        UpdateProductResult result = commandBus.dispatch(command);
        
        return updateProductDtoMapper.toResponse(result);
    }

    public ProductDescriptionsResponse replaceProductDescriptions(String productId, ReplaceProductDescriptionsRequest request) {
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
        return new ProductDescriptionsResponse(result.productId(), mapDescriptions(result));
    }

    public ProductMediaResponse replaceProductMedia(String productId, ReplaceProductMediaRequest request) {
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
        return new ProductMediaResponse(result.productId(), mapMedias(result));
    }

    public UpdateProductStatusResponse updateProductStatus(String productId, UpdateProductStatusRequest request) {
        log.info("Updating product status: {}", productId);

        UpdateProductStatusCommand command = updateProductStatusDtoMapper.toCommand(productId, request);
        UpdateProductStatusResult result = commandBus.dispatch(command);
        
        return updateProductStatusDtoMapper.toResponse(result);
    }

    public ProductModerationResponse moderateProduct(String productId, String action, ProductModerationRequest request) {
        ModerateProductCommand command = new ModerateProductCommand(
                idGenerator.convertIdFrom(productId),
                action,
                request == null ? null : request.reason()
        );
        ModerateProductResult result = commandBus.dispatch(command);
        return new ProductModerationResponse(
                result.productId(),
                result.action(),
                result.oldStatus(),
                result.newStatus(),
                result.reason()
        );
    }

    public BulkUpsertProductsResponse bulkUpsertProducts(BulkUpsertProductsRequest request) {
        List<BulkUpsertProductsResponse.Entry> results = new java.util.ArrayList<>();
        for (SaveProductRequest productRequest : request.products()) {
            String createdId = saveProduct(productRequest);
            results.add(new BulkUpsertProductsResponse.Entry(createdId, "CREATED"));
        }
        return new BulkUpsertProductsResponse(results);
    }

    private List<GetProductResponse.Description> mapDescriptions(ProductDescriptionsResult result) {
        return result.descriptions().stream()
                .map(description -> new GetProductResponse.Description(
                        description.id() == null ? null : description.id().getValue(),
                        description.name(),
                        description.title(),
                        description.description()
                ))
                .toList();
    }

    private List<GetProductResponse.Media> mapMedias(ProductMediaResult result) {
        return result.medias().stream()
                .map(media -> new GetProductResponse.Media(
                        media.id() == null ? null : media.id().getValue(),
                        media.type(),
                        media.path()
                ))
                .toList();
    }
}
