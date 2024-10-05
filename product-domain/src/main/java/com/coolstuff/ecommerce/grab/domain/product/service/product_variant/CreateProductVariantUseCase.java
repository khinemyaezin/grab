package com.coolstuff.ecommerce.grab.domain.product.service.product_variant;

import com.coolstuff.ecommerce.grab.domain.product.entity.product.PersistableProduct;
import com.coolstuff.ecommerce.grab.domain.product.entity.product.Product;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.MultiplePersistableProductVariants;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.PersistableProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ReadableProductVariant;

public interface CreateProductVariantUseCase {
    ReadableProductVariant createProduct(PersistableProductVariant persistableProductVariant);

    void createProductVariants(MultiplePersistableProductVariants multiplePersistableProductVariants);
}
