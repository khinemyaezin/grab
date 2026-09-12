package com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PricingCoverageValidator
        implements ConstraintValidator<ValidPricingCoverage, CreateSellableProductRequest> {

    @Override
    public boolean isValid(CreateSellableProductRequest request, ConstraintValidatorContext context) {
        if (request == null || request.product() == null) {
            return true;
        }

        List<CreateSellableProductRequest.Variant> variants = request.product().variants() == null
                ? List.of()
                : request.product().variants();
        if (variants.isEmpty()) {
            return true;
        }

        Set<String> pricingSkus = new HashSet<>();
        List<CreateSellableProductRequest.PricingLine> lines = request.pricingLines() == null
                ? List.of()
                : request.pricingLines();
        for (CreateSellableProductRequest.PricingLine line : lines) {
            if (line != null && line.sku() != null && !line.sku().isBlank()) {
                pricingSkus.add(line.sku().toUpperCase(Locale.ROOT));
            }
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;
        for (int i = 0; i < variants.size(); i++) {
            CreateSellableProductRequest.Variant variant = variants.get(i);
            if (variant == null || variant.sku() == null || variant.sku().isBlank()) {
                continue;
            }
            if (!pricingSkus.contains(variant.sku().toUpperCase(Locale.ROOT))) {
                valid = false;
                context.buildConstraintViolationWithTemplate("pricing line is required for sku")
                        .addPropertyNode("product")
                        .addPropertyNode("variants")
                        .inIterable()
                        .atIndex(i)
                        .addPropertyNode("sku")
                        .addConstraintViolation();
            }
        }
        return valid;
    }
}
