package com.product.infrastructure.repository.product_variant;

import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.repository.product_variant.ProductVariantRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductVariantFacadeRepository implements ProductVariantRepository {
    private final ProductVariantEntityRepository productVariantEntityRepository;

    public ProductVariantFacadeRepository(ProductVariantEntityRepository productVariantEntityRepository) {
        this.productVariantEntityRepository = productVariantEntityRepository;
    }

    @Override
    public ProductVariant save(ProductVariant productVariant) {
        return null;
    }

    @Override
    public void delete(String id) {

    }
}
