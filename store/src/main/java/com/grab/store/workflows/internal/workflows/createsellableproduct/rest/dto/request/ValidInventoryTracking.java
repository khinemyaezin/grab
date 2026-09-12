package com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InventoryTrackingValidator.class)
public @interface ValidInventoryTracking {

    String message() default "inventory lines must match variant manageInventory flags";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
