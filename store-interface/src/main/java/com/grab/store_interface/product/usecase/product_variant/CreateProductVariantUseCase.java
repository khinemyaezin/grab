package com.grab.store_interface.product.usecase.product_variant;

import com.grab.store_interface.product.dto.product_variant.MultiplePersistableProductVariants;
import com.grab.store_interface.product.dto.product_variant.PersistableProductVariant;
import com.grab.store_interface.product.dto.product_variant.ReadableProductVariant;

public interface CreateProductVariantUseCase {
    ReadableProductVariant createProduct(PersistableProductVariant persistableProductVariant);

    void createProductVariants(MultiplePersistableProductVariants multiplePersistableProductVariants);
}
