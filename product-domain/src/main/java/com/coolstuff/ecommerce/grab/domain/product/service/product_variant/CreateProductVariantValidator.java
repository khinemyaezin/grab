package com.coolstuff.ecommerce.grab.domain.product.service.product_variant;

import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.PersistableProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option.PersistableProductVariantOption;
import com.coolstuff.ecommerce.grab.domain.product.generic.Validator;
import org.springframework.util.Assert;

class CreateProductVariantValidator implements Validator<PersistableProductVariant> {
    @Override
    public void validate(PersistableProductVariant input) throws IllegalArgumentException {
        Assert.hasLength(input.getSku(), "Product name must not be empty");
        Assert.notEmpty(input.getProductVariantOptions(), "Product variant option must not be empty");

        for(PersistableProductVariantOption option: input.getProductVariantOptions()) {
            Assert.hasLength(option.getVariantOptionValue(),"Variant option value must not be empty");
            Assert.hasLength(option.getVariantTypeValue(),"Variant type value must not be empty");
        }
    }
}
