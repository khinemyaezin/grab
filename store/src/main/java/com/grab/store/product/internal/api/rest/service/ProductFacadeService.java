package com.grab.store.product.internal.api.rest.service;

import com.grab.store.product.internal.api.rest.dto.CreateProductRequest;
import com.grab.store.product.internal.api.rest.dto.ResponseDto;
import com.grab.store.product.internal.api.rest.dto.ProductDto;
import com.grab.store.product.internal.api.rest.dto.UpdateProductRequest;
import com.grab.store.product.internal.api.rest.mapper.*;
import com.grab.store.product.internal.command.CreateProductCommand;
import com.grab.store.product.internal.command.UpdateProductCommand;
import com.grab.store.product.internal.service.ProductQueryService;
import com.grab.store.product.internal.usecase.CreateProductUseCase;
import com.grab.store.product.internal.usecase.UpdateProductUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductFacadeService{
    private final CreateProductCommandMapper createProductCommandMapper;
    private final UpdateProductCommandMapper updateProductCommandMapper;
    private final ProductDtoFromUpdateProductCommandMapper productDtoFromUpdateProductCommandMapper;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductQueryService productQueryService;
    private final ProductDtoFromProductMapper productDtoFromProductMapper;


    public ResponseDto createProduct(CreateProductRequest product){
        CreateProductCommand createProductCommand = createProductCommandMapper.map(product);
        createProductUseCase.handle(createProductCommand);
        return new ResponseDto(createProductCommand.id().toString());
    }

    public ResponseDto updateProduct(UpdateProductRequest product){
        UpdateProductCommand updateProductCommand = updateProductCommandMapper.map(product);
        updateProductUseCase.handle(updateProductCommand);
        return new ResponseDto(updateProductCommand.id().toString());
    }

    public Optional<ProductDto> find(String uuid) {
        return this.productQueryService.find(uuid)
                .map(productDtoFromProductMapper::convertProduct);
    }
}
