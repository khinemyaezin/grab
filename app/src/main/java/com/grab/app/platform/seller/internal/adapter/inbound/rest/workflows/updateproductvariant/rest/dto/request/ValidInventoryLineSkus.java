package com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updateproductvariant.dto.request;

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
@Constraint(validatedBy = InventoryLineSkusValidator.class)
public @interface ValidInventoryLineSkus {

    String message() default "inventory line sku must match variant sku";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
