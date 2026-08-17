package com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InventoryLineValidator
        implements ConstraintValidator<ValidInventoryLine, UpdateSellableProductRequest.InventoryLine> {

    @Override
    public boolean isValid(UpdateSellableProductRequest.InventoryLine line, ConstraintValidatorContext context) {
        if (line == null || line.op() == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        return switch (line.op()) {
            case CREATE -> validateCreate(line, context);
            case ADJUST -> validateAdjust(line, context);
            case DAMAGE -> validateDamage(line, context);
            case WRITE_OFF -> validateWriteOff(line, context);
            case REORDER -> validateReorder(line, context);
        };
    }

    private boolean validateCreate(
            UpdateSellableProductRequest.InventoryLine line,
            ConstraintValidatorContext context
    ) {
        boolean valid = true;
        if (!isBlank(line.inventoryItemId())) {
            addViolation(context, "inventoryItemId", "must be absent when op is CREATE");
            valid = false;
        }
        if (isBlank(line.locationId())) {
            addViolation(context, "locationId", "must not be blank when op is CREATE");
            valid = false;
        }
        if (line.create() == null) {
            addViolation(context, "create", "must not be null when op is CREATE");
            valid = false;
        }
        valid &= rejectIfPresent(context, "adjust", line.adjust());
        valid &= rejectIfPresent(context, "damage", line.damage());
        valid &= rejectIfPresent(context, "writeOff", line.writeOff());
        valid &= rejectIfPresent(context, "reorder", line.reorder());
        return valid;
    }

    private boolean validateAdjust(
            UpdateSellableProductRequest.InventoryLine line,
            ConstraintValidatorContext context
    ) {
        boolean valid = requireInventoryItemId(line, context);
        if (line.adjust() == null) {
            addViolation(context, "adjust", "must not be null when op is ADJUST");
            valid = false;
        }
        valid &= rejectIfPresent(context, "create", line.create());
        valid &= rejectIfPresent(context, "damage", line.damage());
        valid &= rejectIfPresent(context, "writeOff", line.writeOff());
        return valid;
    }

    private boolean validateDamage(
            UpdateSellableProductRequest.InventoryLine line,
            ConstraintValidatorContext context
    ) {
        boolean valid = requireInventoryItemId(line, context);
        if (line.damage() == null) {
            addViolation(context, "damage", "must not be null when op is DAMAGE");
            valid = false;
        }
        valid &= rejectIfPresent(context, "create", line.create());
        valid &= rejectIfPresent(context, "adjust", line.adjust());
        valid &= rejectIfPresent(context, "writeOff", line.writeOff());
        return valid;
    }

    private boolean validateWriteOff(
            UpdateSellableProductRequest.InventoryLine line,
            ConstraintValidatorContext context
    ) {
        boolean valid = requireInventoryItemId(line, context);
        if (line.writeOff() == null) {
            addViolation(context, "writeOff", "must not be null when op is WRITE_OFF");
            valid = false;
        }
        valid &= rejectIfPresent(context, "create", line.create());
        valid &= rejectIfPresent(context, "adjust", line.adjust());
        valid &= rejectIfPresent(context, "damage", line.damage());
        return valid;
    }

    private boolean validateReorder(
            UpdateSellableProductRequest.InventoryLine line,
            ConstraintValidatorContext context
    ) {
        boolean valid = requireInventoryItemId(line, context);
        if (line.reorder() == null) {
            addViolation(context, "reorder", "must not be null when op is REORDER");
            valid = false;
        }
        valid &= rejectIfPresent(context, "create", line.create());
        valid &= rejectIfPresent(context, "adjust", line.adjust());
        valid &= rejectIfPresent(context, "damage", line.damage());
        valid &= rejectIfPresent(context, "writeOff", line.writeOff());
        return valid;
    }

    private boolean requireInventoryItemId(
            UpdateSellableProductRequest.InventoryLine line,
            ConstraintValidatorContext context
    ) {
        if (!isBlank(line.inventoryItemId())) {
            return true;
        }
        addViolation(context, "inventoryItemId", "must not be blank when op is " + line.op());
        return false;
    }

    private boolean rejectIfPresent(ConstraintValidatorContext context, String field, Object value) {
        if (value == null) {
            return true;
        }
        addViolation(context, field, "must be absent when op does not use this field");
        return false;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
