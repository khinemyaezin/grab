package com.inventory.adapter.persistence.adapter;

import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.model.read.ProductView;
import com.inventory.adapter.persistence.repository.jpa.ProductVariantViewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductVariantViewQueryAdapter implements ProductVariantViewQueryPort {

    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;

    @Override
    public Optional<ProductView> findActiveBySku(String sku) {
        return productVariantViewJpaRepository.findBySkuAndStatus(sku, ACTIVE_STATUS);
    }

    @Override
    public List<ProductView> findAllBySkuIn(Collection<String> skus) {
        return productVariantViewJpaRepository.findAllBySkuIn(skus);
    }

    @Override
    public List<ProductView> findActiveBySkuContainingIgnoreCase(String sku) {
        return productVariantViewJpaRepository.findBySkuContainingIgnoreCaseAndStatus(sku, ACTIVE_STATUS);
    }
}
