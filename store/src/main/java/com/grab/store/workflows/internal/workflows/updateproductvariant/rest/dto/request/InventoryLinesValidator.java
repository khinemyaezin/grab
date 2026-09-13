package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryLinesValidator
        implements ConstraintValidator<ValidInventoryLines, UpdateProductVariantRequest> {

    @Override
    public boolean isValid(UpdateProductVariantRequest request, ConstraintValidatorContext context) {
        if (request == null || request.inventoryLines() == null) {
            return true;
        }
        Set<String> seen = new HashSet<>();
        boolean valid = true;
        List<UpdateProductVariantRequest.InventoryLine> lines = request.inventoryLines();
        for (int i = 0; i < lines.size(); i++) {
            UpdateProductVariantRequest.InventoryLine line = lines.get(i);
            if (line == null || isBlank(line.inventoryItemId())) {
                continue;
            }
            if (seen.add(line.inventoryItemId())) {
                continue;
            }
            if (valid) {
                context.disableDefaultConstraintViolation();
                valid = false;
            }
            context.buildConstraintViolationWithTemplate("at most one stock operation per inventoryItemId")
                    .addPropertyNode("inventoryLines")
                    .inIterable()
                    .atIndex(i)
                    .addPropertyNode("inventoryItemId")
                    .addConstraintViolation();
        }
        return valid;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
