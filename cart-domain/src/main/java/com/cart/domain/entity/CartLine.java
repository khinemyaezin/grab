package com.cart.domain.entity;

import com.grab.framework.id.Id;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class CartLine {
    private final Id id;
    private final Id variantId;
    private final Id productId;
    private final Id sellerId;
    private final String title;
    private final String sku;
    private BigDecimal unitPrice;
    private int quantity;

    public CartLine(
            Id id,
            Id variantId,
            Id productId,
            Id sellerId,
            String title,
            String sku,
            BigDecimal unitPrice,
            int quantity
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.variantId = Objects.requireNonNull(variantId, "variantId is required");
        this.productId = productId;
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId is required");
        this.title = title;
        this.sku = sku;
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice is required");
        this.quantity = quantity;
    }

    public void replaceQuantityAndPrice(int quantity, BigDecimal unitPrice) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
