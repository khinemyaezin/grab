package com.coolstuff.ecommerce.grab.domain.product.service.product_variant;

import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.MultiplePersistableProductVariants;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.PersistableProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ReadableProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.generic.Validator;
import com.coolstuff.ecommerce.grab.domain.product.repository.product_variant.ProductVariantRepository;

public class CreateProductVariantUseCaseImpl implements CreateProductVariantUseCase {
    private final ProductVariantRepository productVariantRepository;
    private final Validator<PersistableProductVariant> productVariantValidator;

    public CreateProductVariantUseCaseImpl(ProductVariantRepository productVariantRepository, Validator<PersistableProductVariant> productVariantValidator) {
        this.productVariantRepository = productVariantRepository;
        this.productVariantValidator = productVariantValidator;
    }

    @Override
    public ReadableProductVariant createProduct(PersistableProductVariant persistableProductVariant) {
        this.productVariantValidator.validate(persistableProductVariant);
        return this.productVariantRepository.save(persistableProductVariant);
    }

    @Override
    public void createProductVariants(MultiplePersistableProductVariants multiplePersistableProductVariants) {

    }
}
