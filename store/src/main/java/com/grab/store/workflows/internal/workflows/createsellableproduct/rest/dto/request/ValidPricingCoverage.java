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
@Constraint(validatedBy = PricingCoverageValidator.class)
public @interface ValidPricingCoverage {

    String message() default "every variant sku must have a pricing line";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
