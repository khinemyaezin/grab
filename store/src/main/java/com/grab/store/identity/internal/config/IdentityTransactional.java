package com.grab.store.identity.internal.config;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional(transactionManager = "identityTransactionManager")
public @interface IdentityTransactional {
    @AliasFor(annotation = Transactional.class, attribute = "readOnly") boolean readOnly() default false;
}
