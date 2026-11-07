package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

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
@Constraint(validatedBy = UpdateSellableProductPricingLineSkusValidator.class)
public @interface ValidUpdateSellableProductPricingLineSkus {

    String message() default "pricing lines must reference valid skus";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

