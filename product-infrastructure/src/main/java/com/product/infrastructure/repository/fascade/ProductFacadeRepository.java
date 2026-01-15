package com.product.infrastructure.repository.fascade;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.repository.ProductRepository;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.mapper.product.ProductMapper;
import com.product.infrastructure.service.ProductService;
import com.product.infrastructure.service.ProductVariantService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ProductFacadeRepository implements ProductRepository {
    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final DomainEventProducer domainEventProducer;
    private final ProductMapper productMapper;

    @Override
    public void save(Product product) {
        ProductEntity productEntity = productService.findOrBuildProduct(product);
        productVariantService.updateVariants(productEntity, product.getVariants());
        productService.save(productEntity);

        domainEventProducer.produce(product.getEvents());
    }

    @Override
    public void delete(Id uuid) {
        this.productService.find(uuid.getValue())
                .ifPresent(this.productService::delete);
    }

    @Override
    public Optional<Product> find(Id uuid) {
        return this.productService.find(uuid.getValue())
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return this.productService.findAll().stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public Boolean exists(Id uuid) {
        return productService.exists(uuid.getValue());
    }
}
