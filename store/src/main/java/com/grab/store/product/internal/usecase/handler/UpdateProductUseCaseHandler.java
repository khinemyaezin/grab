package com.grab.store.product.internal.usecase.handler;

import com.grab.store.product.internal.annotation.UseCase;
import com.grab.store.product.internal.command.UpdateProductCommand;
import com.grab.store.product.internal.mapper.ProductFromUpdateCommandMapper;
import com.grab.store.product.internal.usecase.UpdateProductUseCase;
import com.product.domain.aggregate.product.Product;
import com.product.domain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@AllArgsConstructor
public class UpdateProductUseCaseHandler implements UpdateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductFromUpdateCommandMapper productMapper;

    @Transactional
    @Override
    public void handle(UpdateProductCommand updateProductCommand) {
        Product updateableProduct = productRepository.find(updateProductCommand.id())
                .orElseThrow(IllegalArgumentException::new);

        productMapper.map(updateableProduct, updateProductCommand);
        productRepository.save(updateableProduct);
    }
}
