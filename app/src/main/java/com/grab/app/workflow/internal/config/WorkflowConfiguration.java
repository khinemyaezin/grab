package com.grab.app.workflow.internal.config;

import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowFingerprint;
import com.grab.framework.workflow.WorkflowTransitionEngine;
import com.grab.framework.workflow.WorkflowDispatchPublisher;
import com.grab.framework.workflow.WorkflowExecutionStore;
import com.grab.framework.workflow.WorkflowHistoryStore;
import com.grab.framework.workflow.WorkflowRuntime;
import com.grab.framework.workflow.WorkflowSignalReceiptStore;
import com.grab.framework.workflow.WorkflowStartReceiptStore;
import com.grab.framework.workflow.WorkflowTimerScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class WorkflowConfiguration {

    @Bean(name = "workflowDefinitionRegistry")
    public WorkflowDefinitionRegistry workflowDefinitionRegistry() {
        return new WorkflowDefinitionRegistry();
    }

    @Bean
    public Clock workflowClock() {
        return Clock.systemUTC();
    }

    @Bean
    public WorkflowRuntime workflowRuntime(
            WorkflowDefinitionRegistry definitions,
            WorkflowExecutionStore executionStore,
            WorkflowStartReceiptStore startReceiptStore,
            WorkflowSignalReceiptStore signalReceiptStore,
            WorkflowHistoryStore historyStore,
            WorkflowDispatchPublisher dispatchPublisher,
            WorkflowTimerScheduler timerScheduler,
            WorkflowFingerprint fingerprint
    ) {
        return new WorkflowRuntime(
                definitions,
                new WorkflowTransitionEngine(fingerprint),
                executionStore,
                startReceiptStore,
                signalReceiptStore,
                historyStore,
                dispatchPublisher,
                timerScheduler,
                fingerprint
        );
    }
}
