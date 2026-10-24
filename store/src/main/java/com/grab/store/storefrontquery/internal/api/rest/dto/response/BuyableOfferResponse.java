package com.grab.store.storefrontquery.internal.api.rest.dto.response;

import java.util.List;
import java.util.Map;

public record BuyableOfferResponse(
        String salesChannelId, String productId, String merchantId, String categoryId, String title, String slug,
        String description, String thumbnail, String productStatus, boolean published, boolean channelEnabled, boolean hasStock,
        List<ProductOption> options, List<VariantFact> variants
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
            boolean allowBackorder, boolean inStock) {
        public VariantFact { optionValues = optionValues == null ? Map.of() : Map.copyOf(optionValues); }
    }
}
