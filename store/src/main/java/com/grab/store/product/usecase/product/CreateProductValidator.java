package com.grab.store.product.usecase.product;

import com.grab.store_interface.product.dto.product.PersistableProduct;
import com.product.domain.generic.Validator;
import org.springframework.stereotype.Component;

@Component
class CreateProductValidator implements Validator<PersistableProduct> {
    @Override
    public void validate(PersistableProduct input) {
        /*Assert.notNull(input.getName(),"Product name must not empty");
        Assert.hasLength(input.getCategoryId(),"Category is required");*/
    }
}
