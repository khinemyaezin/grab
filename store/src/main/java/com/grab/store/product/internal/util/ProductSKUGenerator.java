package com.grab.store.product.internal.util;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.domain.service.SkuGenerator;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application-specific SKU Generator supporting multiple generation policies.
 *
 * This is an application-level policy decision - different applications
 * might have different SKU generation strategies.
 *
 * Policies:
 * - ABBREVIATION: Generates SKU from variation option abbreviations (e.g., "RED-LAR-MAL")
 * - RANDOM: Generates random alphanumeric SKU (e.g., "SKU-A7B3C9D2")
 * - SEQUENTIAL: Uses product name + sequential suffix (e.g., "TSHIRT-001")
 * - HYBRID: Combines product name abbreviation with variation abbreviations
 */
public class ProductSKUGenerator implements SkuGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * SKU generation policy enumeration.
     */
    public enum Policy {
        ABBREVIATION,  // From variation options
        RANDOM,        // Random alphanumeric
        SEQUENTIAL,    // Product name + index
        HYBRID         // Product + variations
    }

    // Default policy - can be made configurable via application.properties
    private Policy defaultPolicy = Policy.ABBREVIATION;

    @Override
    public String generate(Context context) {
        // If baseSku is provided, use it with collision handling
        if (context.baseSku() != null && !context.baseSku().isBlank()) {
            return handleCollision(context.baseSku(), context.collisionIndex());
        }

        // Generate new SKU based on policy
        return generateByPolicy(context, defaultPolicy);
    }

    /**
     * Generate SKU with a specific policy.
     */
    public String generate(Context context, Policy policy) {
        if (context.baseSku() != null && !context.baseSku().isBlank()) {
            return handleCollision(context.baseSku(), context.collisionIndex());
        }
        return generateByPolicy(context, policy);
    }

    /**
     * Generate SKU from user-provided template.
     * Template placeholders:
     * - {product} - Product name abbreviation
     * - {variations} - Variation abbreviations joined by dash
     * - {random} - Random 4-character string
     * - {index} - Collision index
     *
     * Example: "PRD-{product}-{variations}-{random}" → "PRD-TSH-RED-LAR-A7B3"
     */
    public String generateFromTemplate(Context context, String template) {
        String productAbbr = abbreviate(context.productName(), 3);
        String variationsStr = generateFromVariations(context.orderedVariations());
        String randomStr = generateRandomString(4);
        String indexStr = String.format("%03d", context.collisionIndex());

        return template
                .replace("{product}", productAbbr)
                .replace("{variations}", variationsStr)
                .replace("{random}", randomStr)
                .replace("{index}", indexStr);
    }

    private String generateByPolicy(Context context, Policy policy) {
        return switch (policy) {
            case ABBREVIATION -> generateAbbreviationSku(context);
            case RANDOM -> generateRandomSku(context);
            case SEQUENTIAL -> generateSequentialSku(context);
            case HYBRID -> generateHybridSku(context);
        };
    }

    /**
     * ABBREVIATION policy: Generate SKU from variation option abbreviations.
     * Example: Color=Red, Size=Large, Gender=Male → "RED-LAR-MAL"
     */
    private String generateAbbreviationSku(Context context) {
        String baseSku = generateFromVariations(context.orderedVariations());

        if (baseSku.isEmpty()) {
            // Fallback to product name if no variations
            baseSku = abbreviate(context.productName(), 6);
        }

        return handleCollision(baseSku, context.collisionIndex());
    }

    /**
     * RANDOM policy: Generate random alphanumeric SKU.
     * Example: "SKU-A7B3C9D2"
     */
    private String generateRandomSku(Context context) {
        String randomPart = generateRandomString(8);
        String baseSku = "SKU-" + randomPart;
        return handleCollision(baseSku, context.collisionIndex());
    }

    /**
     * SEQUENTIAL policy: Product name abbreviation + collision index.
     * Example: "TSHIRT-001", "TSHIRT-002"
     */
    private String generateSequentialSku(Context context) {
        String productAbbr = abbreviate(context.productName(), 6);
        return String.format("%s-%03d", productAbbr, context.collisionIndex() + 1);
    }

    /**
     * HYBRID policy: Combines product name and variation abbreviations.
     * Example: Product="T-Shirt", Color=Red, Size=L → "TSH-RED-L"
     */
    private String generateHybridSku(Context context) {
        String productAbbr = abbreviate(context.productName(), 3);
        String variationsStr = generateFromVariations(context.orderedVariations());

        String baseSku;
        if (variationsStr.isEmpty()) {
            baseSku = productAbbr;
        } else {
            baseSku = productAbbr + "-" + variationsStr;
        }

        return handleCollision(baseSku, context.collisionIndex());
    }

    /**
     * Generate SKU part from variation options.
     */
    private String generateFromVariations(List<ProductVariation> variations) {
        if (variations == null || variations.isEmpty()) {
            return "";
        }

        return variations.stream()
                .map(v -> abbreviate(v.getOptionName(), 3))
                .collect(Collectors.joining("-"));
    }

    /**
     * Handle collision by appending suffix if needed.
     */
    private String handleCollision(String baseSku, int collisionIndex) {
        if (collisionIndex <= 0) {
            return baseSku;
        }
        return baseSku + "-" + collisionIndex;
    }

    /**
     * Create an abbreviation for SKU generation.
     * Removes non-alphanumeric characters and converts to uppercase.
     */
    private String abbreviate(String name, int maxLength) {
        if (name == null || name.isEmpty()) {
            return "UNK";
        }
        String cleaned = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleaned.isEmpty()) {
            return "UNK";
        }
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    /**
     * Generate random alphanumeric string.
     */
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Set the default policy (can be used for configuration).
     */
    public void setDefaultPolicy(Policy policy) {
        this.defaultPolicy = policy;
    }

    /**
     * Get the current default policy.
     */
    public Policy getDefaultPolicy() {
        return defaultPolicy;
    }
}