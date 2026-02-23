package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateProductStatusModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductStatusRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductStatusResponse;
import com.grab.store.catalog.internal.api.rest.mapper.SaveProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateProductStatusDtoMapper;
import com.grab.store.catalog.internal.command.DeleteProductCommand;
import com.grab.store.catalog.internal.command.DeleteProductResult;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import com.grab.store.catalog.internal.command.SaveProductResult;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.catalog.internal.command.UpdateProductStatusCommand;
import com.grab.store.catalog.internal.command.UpdateProductStatusResult;
import com.grab.store.catalog.internal.cqrs.command.CommandBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCommandService {

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

        SaveProductCommand command = saveProductDtoMapper.toCommand(request);
        SaveProductResult result = commandBus.dispatch(command);

        return result.productId();
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

    public EntityModel<UpdateProductStatusResponse> updateProductStatus(String productId, UpdateProductStatusRequest request) {
        log.info("Updating product status: {}", productId);

        UpdateProductStatusCommand command = updateProductStatusDtoMapper.toCommand(productId, request);
        UpdateProductStatusResult result = commandBus.dispatch(command);
        UpdateProductStatusResponse response = updateProductStatusDtoMapper.toResponse(result);

        return updateProductStatusModelAssembler.toModel(response);
    }
}
