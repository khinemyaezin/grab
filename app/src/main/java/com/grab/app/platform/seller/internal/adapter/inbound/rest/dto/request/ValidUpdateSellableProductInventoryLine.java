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
@Constraint(validatedBy = UpdateSellableProductInventoryLineValidator.class)
public @interface ValidUpdateSellableProductInventoryLine {

    String message() default "invalid inventory line";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

