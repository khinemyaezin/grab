package com.grab.store.product.internal.usecase.handler;

import com.grab.store.product.internal.annotation.UseCase;
import com.grab.store.product.internal.command.CreateProductCommand;
import com.grab.store.product.internal.command.RemoveProductCommand;
import com.grab.store.product.internal.usecase.AbstractPersitUseCase;
import com.grab.store.product.internal.usecase.RemoveProductUseCase;
import com.product.domain.aggregate.product.Product;
import com.product.domain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@AllArgsConstructor
public class RemoveProductUseCaseHandler extends AbstractPersitUseCase<RemoveProductCommand, Void> {
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public Void handle(RemoveProductCommand request) {
        this.productRepository.delete(request.productId());
        return null;
    }
}
