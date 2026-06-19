package com.grab.store.identity.internal.config;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional(transactionManager = "identityTransactionManager", readOnly = true)
public @interface IdentityReadTransactional {
}
