package com.grab.framework.scheduling;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class A scheduled job: safe to run concurrently on every replica because work is claimed
 * row-by-row (e.g. {@code FOR UPDATE SKIP LOCKED}) or is purely node-local. Intentionally unlocked.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompetingWorkerJob {
    /** Why concurrent execution is safe. */
    String value();
}
