package com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request;

import com.grab.store.workflows.events.InventorySyncOp;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class InventoryLinesValidator
        implements ConstraintValidator<ValidInventoryLines, UpdateSellableProductRequest> {

    @Override
    public boolean isValid(UpdateSellableProductRequest request, ConstraintValidatorContext context) {
        if (request == null || request.inventoryLines() == null) {
            return true;
        }
        Set<String> seen = new HashSet<>();
        boolean valid = true;
        List<UpdateSellableProductRequest.InventoryLine> lines = request.inventoryLines();
        Map<String, Boolean> manageBySku = manageInventoryBySku(request);
        for (int i = 0; i < lines.size(); i++) {
            UpdateSellableProductRequest.InventoryLine line = lines.get(i);
            if (line == null) {
                continue;
            }
            if (line.op() == InventorySyncOp.CREATE
                    && line.sku() != null
                    && Boolean.FALSE.equals(manageBySku.get(line.sku().toUpperCase(Locale.ROOT)))) {
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

    private static Map<String, Boolean> manageInventoryBySku(UpdateSellableProductRequest request) {
        Map<String, Boolean> manageBySku = new HashMap<>();
        if (request.product() == null
                || request.product().variantSync() == null
                || request.product().variantSync().overrides() == null) {
            return manageBySku;
        }
        for (UpdateSellableProductRequest.Variant override : request.product().variantSync().overrides()) {
            if (override == null || isBlank(override.sku())) {
                continue;
            }
            manageBySku.put(override.sku().toUpperCase(Locale.ROOT), Boolean.TRUE.equals(override.manageInventory()));
        }
        return manageBySku;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
