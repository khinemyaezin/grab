package com.product.infrastructure.validator.category;

import com.product.domain.aggregate.category.ICategory;
import com.product.infrastructure.validator.Validator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @Throw IllegalArgumentException if it is invalid
 */
@Component
public class PersistableCategoryValidator implements Validator<ICategory> {
    @Override
    public void validate(ICategory data) {
        Assert.notNull(data,"Category object can not be null");
        Assert.hasLength(data.getName(),"Category name must not be empty");
        Assert.hasLength(data.getUuid(),"Category uuid must not be empty");
    }
}
