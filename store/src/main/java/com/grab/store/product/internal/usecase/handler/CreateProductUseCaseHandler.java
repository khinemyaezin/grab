package com.grab.store.product.internal.usecase.handler;

import com.grab.store.product.internal.annotation.UseCase;
import com.grab.store.product.internal.command.CreateProductCommand;
import com.grab.store.product.internal.mapper.ProductFromCreateCommandMapper;
import com.grab.store.product.internal.usecase.AbstractPersitUseCase;
import com.product.domain.aggregate.product.Product;
import com.product.domain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@AllArgsConstructor
public class CreateProductUseCaseHandler extends AbstractPersitUseCase<CreateProductCommand,Product> {
    private final ProductRepository productRepository;
    private final ProductFromCreateCommandMapper productMapper;

    @Transactional
    @Override
    public Product handle(CreateProductCommand createProductCommand) {
        Product product = productMapper.map(createProductCommand);
        product.getId()
                .filter(id -> !productRepository.exists(id))
                .orElseThrow(IllegalArgumentException::new);

        productRepository.save(product);
        return product;
    }
}
