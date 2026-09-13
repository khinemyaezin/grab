package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Locale;

public class InventoryLineSkusValidator
        implements ConstraintValidator<ValidInventoryLineSkus, UpdateProductVariantRequest> {

    @Override
    public boolean isValid(UpdateProductVariantRequest request, ConstraintValidatorContext context) {
        if (request == null || request.inventoryLines() == null || request.inventoryLines().isEmpty()) {
            return true;
        }
        String variantSku = normalize(request.sku());
        if (variantSku == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;
        List<UpdateProductVariantRequest.InventoryLine> lines = request.inventoryLines();
        for (int i = 0; i < lines.size(); i++) {
            UpdateProductVariantRequest.InventoryLine line = lines.get(i);
            if (line == null || line.sku() == null || line.sku().isBlank()) {
                continue;
            }
            if (variantSku.equals(normalize(line.sku()))) {
                continue;
            }
            valid = false;
            context.buildConstraintViolationWithTemplate("inventory line sku must match variant sku")
                    .addPropertyNode("inventoryLines")
                    .inIterable()
                    .atIndex(i)
                    .addPropertyNode("sku")
                    .addConstraintViolation();
        }
        return valid;
    }

    private static String normalize(String sku) {
        if (sku == null || sku.isBlank()) {
            return null;
        }
        return sku.toUpperCase(Locale.ROOT);
    }
}
