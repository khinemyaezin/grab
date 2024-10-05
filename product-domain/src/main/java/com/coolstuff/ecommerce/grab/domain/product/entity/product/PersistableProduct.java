package com.coolstuff.ecommerce.grab.domain.product.entity.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PersistableProduct implements Serializable {
    private String name;
    private String categoryId;
}
