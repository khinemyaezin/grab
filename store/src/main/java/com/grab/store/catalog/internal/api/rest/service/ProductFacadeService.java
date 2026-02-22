package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.api.rest.assembler.GetProductBySlugModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.GetProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductCombinationModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductSummaryModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateProductStatusModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateVariantModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteVariantModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.RestoreVariantModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.SyncVariantsModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.SyncVariantsRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductStatusRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateVariantRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductStatusResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.RestoreVariantResponse;
import com.grab.store.catalog.internal.api.rest.mapper.GetProductBySlugDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.GetProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductCombinationDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductSummaryDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductSummaryQueryMapper;
import com.grab.store.catalog.internal.api.rest.mapper.SaveProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductStatusDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.SyncVariantsDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateVariantDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.DeleteVariantDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.RestoreVariantDtoMapper;
import com.grab.store.catalog.internal.command.DeleteProductCommand;
import com.grab.store.catalog.internal.command.DeleteProductResult;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import com.grab.store.catalog.internal.command.SaveProductResult;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.catalog.internal.command.UpdateProductStatusCommand;
import com.grab.store.catalog.internal.command.UpdateProductStatusResult;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
import com.grab.store.catalog.internal.command.DeleteVariantCommand;
import com.grab.store.catalog.internal.command.DeleteVariantResult;
import com.grab.store.catalog.internal.command.RestoreVariantCommand;
import com.grab.store.catalog.internal.command.RestoreVariantResult;
import com.grab.store.catalog.internal.command.SyncVariantsCommand;
import com.grab.store.catalog.internal.command.SyncVariantsResult;
import com.grab.store.catalog.internal.cqrs.command.CommandBus;
import com.grab.store.catalog.internal.cqrs.query.QueryBus;
import com.grab.store.catalog.internal.query.ProductCombinationQuery;
import com.grab.store.catalog.internal.query.ProductCombinationResult;
import com.grab.store.catalog.internal.query.GetProductQuery;
import com.grab.store.catalog.internal.query.GetProductBySlugResult;
import com.grab.store.catalog.internal.query.GetProductResult;
import com.grab.store.catalog.internal.query.ProductSummaryQuery;
import com.grab.store.catalog.internal.query.GetProductBySlugQuery;
import com.grab.store.catalog.internal.query.GetProductsByCategoryQuery;
import com.grab.store.catalog.internal.query.GetFeaturedProductsQuery;
import com.grab.store.catalog.internal.query.ProductSummaryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFacadeService {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    private final GetProductDtoMapper getProductDtoMapper;
    private final GetProductBySlugDtoMapper getProductBySlugDtoMapper;
    private final GetProductModelAssembler getProductModelAssembler;
    private final GetProductBySlugModelAssembler getProductBySlugModelAssembler;
    private final ProductCombinationDtoMapper productCombinationDtoMapper;
    private final ProductCombinationModelAssembler productCombinationModelAssembler;
    private final ProductSummaryModelAssembler productSummaryModelAssembler;
    private final ProductSummaryQueryMapper productSummaryQueryMapper;
    private final SaveProductDtoMapper saveProductDtoMapper;
    private final DeleteProductModelAssembler deleteProductModelAssembler;
    private final ProductSummaryDtoMapper productSummaryDtoMapper;
    private final UpdateProductDtoMapper updateProductDtoMapper;
    private final UpdateProductStatusDtoMapper updateProductStatusDtoMapper;
    private final UpdateVariantDtoMapper updateVariantDtoMapper;
    private final DeleteVariantDtoMapper deleteVariantDtoMapper;
    private final RestoreVariantDtoMapper restoreVariantDtoMapper;
    private final SyncVariantsDtoMapper syncVariantsDtoMapper;
    private final UpdateProductModelAssembler updateProductModelAssembler;
    private final UpdateProductStatusModelAssembler updateProductStatusModelAssembler;
    private final UpdateVariantModelAssembler updateVariantModelAssembler;
    private final DeleteVariantModelAssembler deleteVariantModelAssembler;
    private final RestoreVariantModelAssembler restoreVariantModelAssembler;
    private final SyncVariantsModelAssembler syncVariantsModelAssembler;
    private final IdGenerator idGenerator;

    public EntityModel<GetProductResponse> getProduct(String productId) {
        log.info("Getting product: {}", productId);

        GetProductQuery query = new GetProductQuery(productId);
        GetProductResult result = queryBus.dispatch(query);
        GetProductResponse response = getProductDtoMapper.toResponse(result);
        return getProductModelAssembler.toModel(response);
    }

    public EntityModel<ProductCombinationResponse> getProductCombination(ProductCombinationRequest request) {
        log.info("Product combination: {}", request.product().name());

        ProductCombinationQuery query = productCombinationDtoMapper.toQuery(request);

        ProductCombinationResult result = queryBus.dispatch(query);

        ProductCombinationResponse response = productCombinationDtoMapper.toResponse(result);

        return productCombinationModelAssembler.toModel(response);
    }

    public String saveProduct(SaveProductRequest request) {
        log.info("Saving product: {}", request.product().name());

        SaveProductCommand command = saveProductDtoMapper.toCommand(request);

        SaveProductResult result = commandBus.dispatch(command);

        return result.productId();
    }

    public EntityModel<ProductSummaryResponse> getProductSummary(ProductSummaryRequest request) {
        log.info("Searching products");

        ProductSummaryQuery query = productSummaryQueryMapper.toQuery(
                request.productName(),
                request.sku(),
                request.variantStatus(),
                request.variations(),
                request.page(),
                request.size()
        );

        ProductSummaryResult result = queryBus.dispatch(query);
        ProductSummaryResponse response = productSummaryDtoMapper.toResponse(result);
        return productSummaryModelAssembler.toModel(response);
    }

    public EntityModel<DeleteProductResponse> deleteProduct(String productId) {
        log.info("Deleting product: {}", productId);

        DeleteProductCommand command = new DeleteProductCommand(idGenerator.generateId(productId));

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

    public EntityModel<UpdateVariantResponse> updateVariant(String productId, String variantId, UpdateVariantRequest request) {
        log.info("Updating variant: {} for product: {}", variantId, productId);

        UpdateVariantCommand command = updateVariantDtoMapper.toCommand(productId, variantId, request);
        UpdateVariantResult result = commandBus.dispatch(command);
        UpdateVariantResponse response = updateVariantDtoMapper.toResponse(result);

        return updateVariantModelAssembler.toModel(response);
    }

    public EntityModel<UpdateProductStatusResponse> updateProductStatus(String productId, UpdateProductStatusRequest request) {
        log.info("Updating product status: {}", productId);

        UpdateProductStatusCommand command = updateProductStatusDtoMapper.toCommand(productId, request);
        UpdateProductStatusResult result = commandBus.dispatch(command);
        UpdateProductStatusResponse response = updateProductStatusDtoMapper.toResponse(result);

        return updateProductStatusModelAssembler.toModel(response);
    }

    public EntityModel<DeleteVariantResponse> deleteVariant(String productId, String variantId) {
        log.info("Deleting variant: {} for product: {}", variantId, productId);

        DeleteVariantCommand command = deleteVariantDtoMapper.toCommand(productId, variantId);
        DeleteVariantResult result = commandBus.dispatch(command);
        DeleteVariantResponse response = deleteVariantDtoMapper.toResponse(result);

        return deleteVariantModelAssembler.toModel(response);
    }

    public EntityModel<ProductSummaryResponse> getProductsByCategory(String categoryId, int page, int size) {
        log.info("Getting products by category: {}", categoryId);

        GetProductsByCategoryQuery query = new GetProductsByCategoryQuery(categoryId, page, size);
        ProductSummaryResult result = queryBus.dispatch(query);
        ProductSummaryResponse response = productSummaryDtoMapper.toResponse(result);

        return productSummaryModelAssembler.toModel(response);
    }

    public EntityModel<RestoreVariantResponse> restoreVariant(String productId, String variantId) {
        log.info("Restoring variant: {} for product: {}", variantId, productId);

        RestoreVariantCommand command = restoreVariantDtoMapper.toCommand(productId, variantId);
        RestoreVariantResult result = commandBus.dispatch(command);
        RestoreVariantResponse response = restoreVariantDtoMapper.toResponse(result);

        return restoreVariantModelAssembler.toModel(response);
    }

    public EntityModel<SyncVariantsResponse> syncVariants(String productId, SyncVariantsRequest request) {
        log.info("Syncing variants for product: {}", productId);

        SyncVariantsCommand command = syncVariantsDtoMapper.toCommand(productId, request);
        SyncVariantsResult result = commandBus.dispatch(command);
        SyncVariantsResponse response = syncVariantsDtoMapper.toResponse(result);

        return syncVariantsModelAssembler.toModel(response);
    }

    public EntityModel<GetProductBySlugResponse> getProductBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);

        GetProductBySlugQuery query = new GetProductBySlugQuery(slug);
        GetProductBySlugResult result = queryBus.dispatch(query);
        GetProductBySlugResponse response = getProductBySlugDtoMapper.toResponse(result);

        return getProductBySlugModelAssembler.toModel(response);
    }

    public EntityModel<ProductSummaryResponse> getFeaturedProducts(int page, int size) {
        log.info("Getting featured products");

        GetFeaturedProductsQuery query = new GetFeaturedProductsQuery(page, size);
        ProductSummaryResult result = queryBus.dispatch(query);
        ProductSummaryResponse response = productSummaryDtoMapper.toResponse(result);

        return productSummaryModelAssembler.toModel(response);
    }
}
