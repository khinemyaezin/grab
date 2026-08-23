package com.grab.store.shared.tracing;

import com.grab.framework.logger.slf4j.TraceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ScheduledTraceContextAspect {

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object aroundScheduled(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceContext.put(TraceContext.generate());
        try {
            return joinPoint.proceed();
        } finally {
            TraceContext.clear();
        }
    }
}
