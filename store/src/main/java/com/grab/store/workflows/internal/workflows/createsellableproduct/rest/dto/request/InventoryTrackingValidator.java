package com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InventoryTrackingValidator
        implements ConstraintValidator<ValidInventoryTracking, CreateSellableProductRequest> {

    @Override
    public boolean isValid(CreateSellableProductRequest request, ConstraintValidatorContext context) {
        if (request == null || request.product() == null) {
            return true;
        }

        List<CreateSellableProductRequest.Variant> variants = request.product().variants() == null
                ? List.of()
                : request.product().variants();
        List<CreateSellableProductRequest.InventoryLine> lines = request.inventoryLines() == null
                ? List.of()
                : request.inventoryLines();

        Set<String> lineSkus = new HashSet<>();
        for (CreateSellableProductRequest.InventoryLine line : lines) {
            if (line != null && line.sku() != null && !line.sku().isBlank()) {
                lineSkus.add(line.sku().toUpperCase(Locale.ROOT));
            }
        }

        context.disableDefaultConstraintViolation();

        if (variants.isEmpty()) {
            return true;
        }

        boolean valid = true;
        for (int i = 0; i < variants.size(); i++) {
            CreateSellableProductRequest.Variant variant = variants.get(i);
            if (variant == null || variant.sku() == null || variant.sku().isBlank()) {
                continue;
            }
            boolean managed = Boolean.TRUE.equals(variant.manageInventory());
            boolean hasLine = lineSkus.contains(variant.sku().toUpperCase(Locale.ROOT));
            if (managed && !hasLine) {
                valid = false;
                context.buildConstraintViolationWithTemplate(
                                "inventory lines are required when inventory is managed")
                        .addPropertyNode("product")
                        .addPropertyNode("variants")
                        .inIterable()
                        .atIndex(i)
                        .addPropertyNode("sku")
                        .addConstraintViolation();
            }
            if (!managed && hasLine) {
                valid = false;
                context.buildConstraintViolationWithTemplate(
                                "inventory lines must be absent when inventory is not managed")
                        .addPropertyNode("inventoryLines")
                        .addConstraintViolation();
            }
        }
        return valid;
    }
}
