package com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request;

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
@Constraint(validatedBy = InventoryLinesValidator.class)
public @interface ValidInventoryLines {

    String message() default "at most one stock operation per inventoryItemId";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
