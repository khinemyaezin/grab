package com.inventory.application.port.outbound;

import com.inventory.application.model.read.ProductView;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantViewQueryPort {

    String ACTIVE_STATUS = "ACTIVE";

    Optional<ProductView> findActiveBySku(String sku);

    List<ProductView> findAllBySkuIn(Collection<String> skus);

    List<ProductView> findActiveBySkuContainingIgnoreCase(String sku);
}
