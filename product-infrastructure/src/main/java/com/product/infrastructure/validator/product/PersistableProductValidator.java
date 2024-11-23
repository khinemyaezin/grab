package com.product.infrastructure.validator.product;

import com.product.domain.entity.category.ICategory;
import com.product.domain.entity.product.Product;
import com.product.infrastructure.validator.Validator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @Throw IllegalArgumentException if it is invalid
 */
@Component
public class PersistableProductValidator implements Validator<Product> {
    @Override
    public void validate(Product data) {
        Assert.notNull(data,"Product object can not be null");
        //Assert.notNull(data.getCategory(),"Category object can not be null");
        //Assert.hasLength(data.getUuid(),"Product uuid must not be empty");
    }
}
