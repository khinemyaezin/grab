package com.grab.store.shared.security;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {
                OptimisticLockingFailureException.class,
                DataIntegrityViolationException.class,
                OptimisticLockException.class,
                EntityExistsException.class,
                ConstraintViolationException.class,
                StaleStateException.class
        },
        backoff = @Backoff(delay = 50, multiplier = 2, maxDelay = 200, random = true))
public @interface SecurityManifestPublicationRetryable {
}
