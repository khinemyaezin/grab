package com.coolstuff.ecommerce.grab.domain.product.service.product;

import com.coolstuff.ecommerce.grab.domain.product.entity.product.PersistableProduct;
import com.coolstuff.ecommerce.grab.domain.product.generic.Validator;
import org.springframework.util.Assert;

class CreateProductValidator implements Validator<PersistableProduct> {
    @Override
    public void validate(PersistableProduct input) {
        Assert.notNull(input.getName(),"Product name must not empty");
        Assert.hasLength(input.getCategoryId(),"Category is required");
    }
}
