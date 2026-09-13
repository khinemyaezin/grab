package com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PricingLineSkusValidator
        implements ConstraintValidator<ValidPricingLineSkus, UpdateSellableProductRequest> {

    @Override
    public boolean isValid(UpdateSellableProductRequest request, ConstraintValidatorContext context) {
        if (request == null || request.pricingLines() == null || request.pricingLines().isEmpty()) {
            return true;
        }
        if (!requiresOverrideCoverage(request)) {
            return true;
        }

        Set<String> overrideSkus = overrideSkus(request);
        context.disableDefaultConstraintViolation();
        boolean valid = true;
        List<UpdateSellableProductRequest.PricingLine> lines = request.pricingLines();
        for (int i = 0; i < lines.size(); i++) {
            UpdateSellableProductRequest.PricingLine line = lines.get(i);
            if (line == null || resolvesToVariant(line, overrideSkus)) {
                continue;
            }
            valid = false;
            context.buildConstraintViolationWithTemplate("pricing line sku must match a variant override")
                    .addPropertyNode("pricingLines")
                    .inIterable()
                    .atIndex(i)
                    .addPropertyNode("sku")
                    .addConstraintViolation();
        }
        return valid;
    }

    private static boolean requiresOverrideCoverage(UpdateSellableProductRequest request) {
        if (request.product() == null || request.product().variantSync() == null) {
            return false;
        }
        String intent = request.product().variantSync().intent();
        if (intent == null || intent.isBlank()) {
            return false;
        }
        return "FULL_SYNC".equalsIgnoreCase(intent) || "COLLAPSE_TO_STANDALONE".equalsIgnoreCase(intent);
    }

    private static Set<String> overrideSkus(UpdateSellableProductRequest request) {
        Set<String> skus = new HashSet<>();
        List<UpdateSellableProductRequest.Variant> overrides = request.product().variantSync().overrides();
        if (overrides == null) {
            return skus;
        }
        for (UpdateSellableProductRequest.Variant override : overrides) {
            if (override == null || override.sku() == null || override.sku().isBlank()) {
                continue;
            }
            skus.add(override.sku().toUpperCase(Locale.ROOT));
        }
        return skus;
    }

    private static boolean resolvesToVariant(
            UpdateSellableProductRequest.PricingLine line,
            Set<String> overrideSkus
    ) {
        if (line.variantId() != null && !line.variantId().isBlank()) {
            return true;
        }
        return line.sku() != null && overrideSkus.contains(line.sku().toUpperCase(Locale.ROOT));
    }
}
