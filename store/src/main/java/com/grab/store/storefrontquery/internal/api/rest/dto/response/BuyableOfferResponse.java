package com.grab.store.storefrontquery.internal.api.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.server.core.Relation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Relation(collectionRelation = "buyableOfferResponseList", itemRelation = "buyableOfferResponse")
public record BuyableOfferResponse(
        String salesChannelId, String productId, String merchantId, String categoryId, String title, String slug,
        String description, String thumbnail, String productStatus, boolean published, boolean channelEnabled, boolean hasStock,
        List<ProductOption> options, List<VariantFact> variants,
        String regionId, String countryCode, String currencyCode, String locale, boolean regionServed,
        boolean buyable, @JsonProperty("min_price") String minPrice, @JsonProperty("max_price") String maxPrice
) {
    public BuyableOfferResponse {
        options = options == null ? List.of() : List.copyOf(options);
        variants = variants == null ? List.of() : List.copyOf(variants);
    }
    public record ProductOption(String name, List<String> values) {
        public ProductOption { values = values == null ? List.of() : List.copyOf(values); }
    }
    public record VariantFact(String variantId, String sku, String title, String status,
            Map<String, String> optionValues, int availableQty, boolean manageInventory,
            boolean allowBackorder, boolean inStock, String availability, boolean buyable,
            String amount, String originalAmount) {
        public VariantFact {
            optionValues = optionValues == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(optionValues));
        }
    }
}
