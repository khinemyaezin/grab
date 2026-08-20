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
@Constraint(validatedBy = InventoryLineValidator.class)
public @interface ValidInventoryLine {

    String message() default "inventory line does not match op";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
