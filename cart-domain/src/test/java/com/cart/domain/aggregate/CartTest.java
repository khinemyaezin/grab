package com.cart.domain.aggregate;

import com.cart.domain.entity.CartLine;
import com.cart.domain.exception.CartDomainException;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    @Test
    void addOrIncrease_shouldSnapshotPriceAndAccumulateQuantity() {
        Cart cart = Cart.create(
                new CommonId("cart-1"),
                new CommonId("ch-1"),
                "MARKETPLACE",
                new CommonId("region-1"),
                "MMK",
                "guest-1",
                Instant.now()
        );

        cart.addOrIncrease(
                new CommonId("line-1"),
                new CommonId("var-1"),
                new CommonId("prod-1"),
                new CommonId("seller-1"),
                "Shirt",
                "SKU-1",
                new BigDecimal("10000"),
                1,
                Instant.now()
        );
        CartLine line = cart.addOrIncrease(
                new CommonId("line-2"),
                new CommonId("var-1"),
                new CommonId("prod-1"),
                new CommonId("seller-1"),
                "Shirt",
                "SKU-1",
                new BigDecimal("12000"),
                1,
                Instant.now()
        );

        assertThat(line.getQuantity()).isEqualTo(2);
        assertThat(line.getUnitPrice()).isEqualByComparingTo("12000");
    }

    @Test
    void addOrIncrease_shouldRejectMixedSellerOnWebsite() {
        Cart cart = Cart.create(
                new CommonId("cart-1"),
                new CommonId("ch-1"),
                "WEBSITE",
                new CommonId("region-1"),
                "MMK",
                "guest-1",
                Instant.now()
        );
        cart.addOrIncrease(
                new CommonId("line-1"),
                new CommonId("var-1"),
                new CommonId("prod-1"),
                new CommonId("seller-1"),
                "Shirt",
                "SKU-1",
                new BigDecimal("10000"),
                1,
                Instant.now()
        );

        assertThatThrownBy(() -> cart.addOrIncrease(
                new CommonId("line-2"),
                new CommonId("var-2"),
                new CommonId("prod-2"),
                new CommonId("seller-2"),
                "Hat",
                "SKU-2",
                new BigDecimal("5000"),
                1,
                Instant.now()
        )).isInstanceOf(CartDomainException.class);
    }
}
