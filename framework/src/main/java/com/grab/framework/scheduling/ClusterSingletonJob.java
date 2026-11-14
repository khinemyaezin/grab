package com.grab.framework.scheduling;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class B scheduled job: must run at most once per interval across the cluster.
 * The annotated method MUST execute its body through a {@link SingletonJobExecutor}
 * (backed by a per-schema ShedLock provider in the app). Enforced by architecture tests.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ClusterSingletonJob {
    /** Lock name, e.g. {@code catalog.outbox.cleanup}. */
    String value();
}
