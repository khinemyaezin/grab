package com.grab.discovery.application.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryOfferPolicyTest {

    private DiscoveryOfferPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DiscoveryOfferPolicy();
    }

    @Test
    void isOfferInStock_untracked_returnsTrue() {
        boolean inStock = policy.isOfferInStock(0, true);
        assertThat(inStock).isTrue();
    }

    @Test
    void isOfferInStock_trackedWithPositiveQty_returnsTrue() {
        boolean inStock = policy.isOfferInStock(5, false);
        assertThat(inStock).isTrue();
    }

    @Test
    void isOfferInStock_trackedWithZeroQty_returnsFalse() {
        boolean inStock = policy.isOfferInStock(0, false);
        assertThat(inStock).isFalse();
    }

    @Test
    void isOfferBuyable_allCriteriaSatisfied_returnsTrue() {
        BigDecimal price = new BigDecimal("1000");
        boolean buyable = policy.isOfferBuyable(true, "ACTIVE", true, price, "MMK", "MMK", true);
        assertThat(buyable).isTrue();
    }

    @Test
    void isOfferBuyable_currencyMismatch_returnsFalse() {
        BigDecimal price = new BigDecimal("1000");
        boolean buyable = policy.isOfferBuyable(true, "ACTIVE", true, price, "USD", "MMK", true);
        assertThat(buyable).isFalse();
    }

    @Test
    void isOfferBuyable_productNotActive_returnsFalse() {
        BigDecimal price = new BigDecimal("1000");
        boolean buyable = policy.isOfferBuyable(true, "DRAFT", true, price, "MMK", "MMK", true);
        assertThat(buyable).isFalse();
    }

    @Test
    void isOfferBuyable_channelDisabled_returnsFalse() {
        BigDecimal price = new BigDecimal("1000");
        boolean buyable = policy.isOfferBuyable(true, "ACTIVE", false, price, "MMK", "MMK", true);
        assertThat(buyable).isFalse();
    }

    @Test
    void isOfferBuyable_notInStock_returnsFalse() {
        BigDecimal price = new BigDecimal("1000");
        boolean buyable = policy.isOfferBuyable(true, "ACTIVE", true, price, "MMK", "MMK", false);
        assertThat(buyable).isFalse();
    }

    @Test
    void isProductVisible_enabledActiveAndPublished_returnsTrue() {
        boolean visible = policy.isProductVisible(true, "ACTIVE", true);
        assertThat(visible).isTrue();
    }

    @Test
    void isProductVisible_channelDisabled_returnsFalse() {
        boolean visible = policy.isProductVisible(false, "ACTIVE", true);
        assertThat(visible).isFalse();
    }

    @Test
    void isProductVisible_notPublished_returnsFalse() {
        boolean visible = policy.isProductVisible(true, "ACTIVE", false);
        assertThat(visible).isFalse();
    }

    @Test
    void isProductActive_activeStatus_returnsTrue() {
        boolean active = policy.isProductActive("ACTIVE");
        assertThat(active).isTrue();
    }

    @Test
    void isProductActive_nonActiveStatus_returnsFalse() {
        boolean active = policy.isProductActive("ARCHIVED");
        assertThat(active).isFalse();
    }
}
