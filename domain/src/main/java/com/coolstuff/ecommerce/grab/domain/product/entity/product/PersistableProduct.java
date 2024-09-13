package com.coolstuff.ecommerce.grab.domain.product.entity.product;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.Category;

import java.io.Serializable;

@Getter
@Setter
public class PersistableProduct implements Serializable {
    private String name;

    private Category category;
}
