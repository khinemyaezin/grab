package com.grab.store.product.internal.api.rest.service;

import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.api.rest.assembler.BuildProductModelAssembler;
import com.grab.store.product.internal.api.rest.assembler.DeleteProductModelAssembler;
import com.grab.store.product.internal.api.rest.assembler.GetAllProductsModelAssembler;
import com.grab.store.product.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.product.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.product.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.product.internal.api.rest.dto.response.DeleteProductResponse;
import com.grab.store.product.internal.api.rest.dto.response.GetAllProductsResponse;
import com.grab.store.product.internal.api.rest.mapper.ProductCombinationDtoMapper;
import com.grab.store.product.internal.api.rest.mapper.GetAllProductsDtoMapper;
import com.grab.store.product.internal.api.rest.mapper.SaveProductDtoMapper;
import com.grab.store.product.internal.command.DeleteProductCommand;
import com.grab.store.product.internal.command.DeleteProductResult;
import com.grab.store.product.internal.command.SaveProductCommand;
import com.grab.store.product.internal.command.SaveProductResult;
import com.grab.store.product.internal.cqrs.command.CommandBus;
import com.grab.store.product.internal.cqrs.query.QueryBus;
import com.grab.store.product.internal.query.ProductCombinationQuery;
import com.grab.store.product.internal.query.ProductCombinationResult;
import com.grab.store.product.internal.query.GetAllProductsQuery;
import com.grab.store.product.internal.query.GetAllProductsResult;
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

    private final ProductCombinationDtoMapper combinationDtoMapper;

    private final ProductCombinationDtoMapper productCombinationDtoMapper;
    private final BuildProductModelAssembler buildProductModelAssembler;

    private final SaveProductDtoMapper saveProductDtoMapper;

    private final GetAllProductsDtoMapper getAllProductsDtoMapper;
    private final GetAllProductsModelAssembler getAllProductsModelAssembler;

    private final DeleteProductModelAssembler deleteProductModelAssembler;

    private final IdGenerator idGenerator;

    public EntityModel<ProductCombinationResponse> getProductCombination(ProductCombinationRequest request) {
        log.info("Product combination: {}", request.product().name());

        ProductCombinationQuery query = productCombinationDtoMapper.toQuery(request);

        ProductCombinationResult result = queryBus.dispatch(query);

        ProductCombinationResponse response = productCombinationDtoMapper.toResponse(result);

        return buildProductModelAssembler.toModel(response);
    }

    public String saveProduct(SaveProductRequest request) {
        log.info("Saving product: {}", request.product().name());

        SaveProductCommand command = saveProductDtoMapper.toCommand(request);

        SaveProductResult result = commandBus.dispatch(command);

        return result.productId();
    }

    public EntityModel<GetAllProductsResponse> getAllProducts() {
        log.info("Getting all products");

        GetAllProductsQuery query = new GetAllProductsQuery();

        GetAllProductsResult result = queryBus.dispatch(query);

        GetAllProductsResponse response = getAllProductsDtoMapper.toResponse(result);

        return getAllProductsModelAssembler.toModel(response);
    }

    public EntityModel<DeleteProductResponse> deleteProduct(String productId) {
        log.info("Deleting product: {}", productId);

        DeleteProductCommand command = new DeleteProductCommand(productId);

        DeleteProductResult result = commandBus.dispatch(command);

        DeleteProductResponse response = new DeleteProductResponse(productId, result.deleted());

        return deleteProductModelAssembler.toModel(response);
    }
}
