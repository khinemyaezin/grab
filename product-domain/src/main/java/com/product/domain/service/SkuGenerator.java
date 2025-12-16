package com.product.domain.service;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.ProductVariation;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generates a SKU for a variant in a predictable and collision-safe way.
 * <p>
 * Responsibilities:
 * - Normalize input (casing/whitespace) and apply formatting rules.
 * - Disambiguate collisions via a deterministic suffix strategy (e.g. counter).
 * - Avoid stateful dependencies: callers pass any counter/index they want reflected in the SKU.
 * </p>
 *
 * Real-world usage:
 * <pre>
 * var context = new SkuGenerator.Context(
 *         productId,
 *         "Unisex Tee",
 *         orderedVariations,   // e.g. [Storage=128, Size=Large, Color=Red, Gender=Male]
 *         baseSku,             // existing SKU if extending a variant, otherwise null
 *         collisionIndex,      // 0 for first use, 1+ for additional SKUs sharing the same base
 *         Map.of("channel", "amazon", "region", "sg") // optional, arbitrary metadata
 * );
 * String sku = skuGenerator.generate(context);
 * </pre>
 */
public interface SkuGenerator {

    /**
     * Builds a SKU string using the provided context.
     * Implementations should be deterministic and side-effect free.
     */
    String generate(Context context);

    /**
     * Immutable context for SKU generation.
     */
    @Builder
    record Context(
            Id productId,
            String productName,
            List<ProductVariation> orderedVariations,
            String baseSku,
            int collisionIndex,
            Map<String, String> attributes
    ) {
        public Context {
            Objects.requireNonNull(orderedVariations, "orderedVariations is required");
            if (collisionIndex < 0) {
                throw new IllegalArgumentException("collisionIndex must be >= 0");
            }
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }
}
