package com.grab.discovery.application.policy;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ContextualOfferPolicyTest {
    private final ContextualOfferPolicy policy = new ContextualOfferPolicy();

    @Test
    void compose_servedMarketAppliesAvailabilityPrecedenceAndRanges() {
        DiscoveryProductView product = product(
                variant("v-available", 2, true, false),
                variant("v-sold-out", 0, true, false),
                variant("v-untracked", 0, false, false),
                variant("v-backorder", 0, true, true)
        );
        Map<Id, ContextualOfferPolicy.Quote> prices = Map.of(
                new CommonId("v-available"), quote("10.00"),
                new CommonId("v-sold-out"), quote("0.00"),
                new CommonId("v-untracked"), new ContextualOfferPolicy.Quote(null, null, "SGD"),
                new CommonId("v-backorder"), quote("12.50")
        );

        BuyableOfferResult result = policy.compose(product, context(), true, prices);

        assertThat(result.buyable()).isTrue();
        assertThat(result.minPrice()).isEqualTo("0.00");
        assertThat(result.maxPrice()).isEqualTo("12.50");
        assertThat(result.variants()).extracting(BuyableOfferResult.VariantOffer::availability)
                .containsExactly("AVAILABLE", "SOLD_OUT", "PRICE_UNAVAILABLE", "AVAILABLE");
        assertThat(result.variants().get(1).amount()).isEqualTo("0.00");
    }

    @Test
    void compose_unservedMarketSkipsPricesAndMarksEveryVariantUnavailable() {
        DiscoveryProductView product = product(variant("v-1", 4, true, false));

        BuyableOfferResult result = policy.compose(product, context(), false, Map.of());

        assertThat(result.buyable()).isFalse();
        assertThat(result.minPrice()).isNull();
        assertThat(result.maxPrice()).isNull();
        assertThat(result.variants().getFirst().availability()).isEqualTo("NOT_AVAILABLE_IN_REGION");
        assertThat(result.variants().getFirst().amount()).isNull();
    }

    @Test
    void compose_unpricedQuoteDoesNotExposeAnOriginalAmount() {
        DiscoveryProductView product = product(variant("v-1", 4, true, false));
        Map<Id, ContextualOfferPolicy.Quote> prices = Map.of(
                new CommonId("v-1"),
                new ContextualOfferPolicy.Quote(null, new BigDecimal("12.00"), "SGD")
        );

        BuyableOfferResult result = policy.compose(product, context(), true, prices);

        assertThat(result.variants().getFirst().availability()).isEqualTo("PRICE_UNAVAILABLE");
        assertThat(result.variants().getFirst().amount()).isNull();
        assertThat(result.variants().getFirst().originalAmount()).isNull();
    }

    private ShoppingContext context() {
        return new ShoppingContext(
                new CommonId("channel-1"),
                new CommonId("region-1"),
                "SG",
                "SGD",
                "en-SG"
        );
    }

    private DiscoveryProductView product(DiscoveryProductView.VariantFact... variants) {
        return new DiscoveryProductView(
                new CommonId("channel-1"),
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                new CommonId("category-1"),
                "Product",
                "product",
                null,
                null,
                "ACTIVE",
                true,
                true,
                true,
                List.of(),
                List.of(variants)
        );
    }

    private DiscoveryProductView.VariantFact variant(
            String id,
            int quantity,
            boolean manageInventory,
            boolean allowBackorder
    ) {
        return new DiscoveryProductView.VariantFact(
                new CommonId(id),
                id,
                id,
                "ACTIVE",
                Map.of(),
                quantity,
                manageInventory,
                allowBackorder,
                quantity > 0 || allowBackorder
        );
    }

    private ContextualOfferPolicy.Quote quote(String amount) {
        return new ContextualOfferPolicy.Quote(new BigDecimal(amount), null, "SGD");
    }
}
