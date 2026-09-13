package com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.dto.request;

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
@Constraint(validatedBy = PricingLineSkusValidator.class)
public @interface ValidPricingLineSkus {

    String message() default "pricing line sku must match a variant override";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
