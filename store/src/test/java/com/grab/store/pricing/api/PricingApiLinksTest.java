package com.grab.store.pricing.api;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.assertj.core.api.Assertions.assertThat;

class PricingApiLinksTest {

    @Test
    void listVariantPriceLinks_shouldUseExpectedRelAndHref() {
        Link link = PricingApiLinks.listVariantPriceLinks();

        assertThat(link.getRel().value()).isEqualTo("list-variant-price-links");
        assertThat(link.getHref()).startsWith("/api/v1/pricing/variant-price-links");
    }

    @Test
    void calculatePrices_shouldUseExpectedRelAndHref() {
        Link link = PricingApiLinks.calculatePrices();

        assertThat(link.getRel().value()).isEqualTo("calculate-prices");
        assertThat(link.getHref()).endsWith("/api/v1/pricing/price-sets/calculate");
    }
}
