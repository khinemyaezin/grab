package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.api.rest.assembler.GetProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductCombinationModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductSummaryModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.catalog.internal.api.rest.mapper.GetProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductCombinationDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductSummaryDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductSummaryQueryMapper;
import com.grab.store.catalog.internal.api.rest.mapper.SaveProductDtoMapper;
import com.grab.store.catalog.internal.command.DeleteProductCommand;
import com.grab.store.catalog.internal.command.DeleteProductResult;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import com.grab.store.catalog.internal.command.SaveProductResult;
import com.grab.store.catalog.internal.cqrs.command.CommandBus;
import com.grab.store.catalog.internal.cqrs.query.QueryBus;
import com.grab.store.catalog.internal.query.ProductCombinationQuery;
import com.grab.store.catalog.internal.query.ProductCombinationResult;
import com.grab.store.catalog.internal.query.GetProductQuery;
import com.grab.store.catalog.internal.query.GetProductResult;
import com.grab.store.catalog.internal.query.ProductSummaryQuery;
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
    private final GetProductModelAssembler getProductModelAssembler;
    private final ProductCombinationDtoMapper productCombinationDtoMapper;
    private final ProductCombinationModelAssembler productCombinationModelAssembler;
    private final ProductSummaryModelAssembler productSummaryModelAssembler;
    private final ProductSummaryQueryMapper productSummaryQueryMapper;
    private final SaveProductDtoMapper saveProductDtoMapper;
    private final DeleteProductModelAssembler deleteProductModelAssembler;
    private final ProductSummaryDtoMapper productSummaryDtoMapper;
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
}
