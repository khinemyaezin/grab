package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.request;

import com.grab.store.workflows.events.InventorySyncOp;
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
            if (line == null) {
                continue;
            }
            boolean creatingUntrackedInventory = line.op() == InventorySyncOp.CREATE
                    && Boolean.FALSE.equals(request.manageInventory());
            if (creatingUntrackedInventory) {
                if (valid) {
                    context.disableDefaultConstraintViolation();
                    valid = false;
                }
                context.buildConstraintViolationWithTemplate(
                                "cannot create inventory when manageInventory is false")
                        .addPropertyNode("inventoryLines")
                        .inIterable()
                        .atIndex(i)
                        .addPropertyNode("sku")
                        .addConstraintViolation();
            }
            if (isBlank(line.inventoryItemId())) {
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
