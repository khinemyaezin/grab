package com.product.infrastructure.repository.product;

import com.product.domain.entity.product.Product;
import com.product.domain.repository.product.ProductRepository;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.factory.ProductAssembler;
import com.product.infrastructure.repository.category.CategoryEntityRepository;
import com.product.infrastructure.service.CategoryEntityService;
import com.product.infrastructure.service.ProductEntityService;
import com.product.infrastructure.service.ProductVariantEntityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductFacadeRepository implements ProductRepository {
    private final ProductEntityService productEntityService;
    private final ProductVariantEntityService productVariantEntityService;
    private final CategoryEntityService categoryEntityService;
    private final DomainEventProducer domainEventProducer;
    private final ProductAssembler productAssembler;


    @Transactional
    @Override
    public Product save(Product product) {
        CategoryEntity categoryEntity = categoryEntityService.find(product.getCategoryId()).orElseThrow();
        ProductEntity productEntity = productEntityService.findOrCreateProduct(product, categoryEntity);

        productVariantEntityService.updateVariants(productEntity, product.getVariants());
        productEntityService.save(productEntity);

        domainEventProducer.produce(product.getEvents());
        return product;
    }

    @Transactional
    @Override
    public void delete(String uuid) {
        this.productEntityService.find(uuid)
                .ifPresent(this.productEntityService::delete);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Product> find(String uuid) {
        return this.productEntityService.find(uuid)
                .map(productAssembler::assemble);

    }
}
