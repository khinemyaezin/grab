package com.grab.workflow.infrastructure.config;

import com.grab.framework.workflow.WorkflowExecutionStore;
import com.grab.framework.workflow.WorkflowHistoryStore;
import com.grab.framework.workflow.WorkflowQueryStore;
import com.grab.framework.workflow.WorkflowSignalReceiptStore;
import com.grab.framework.workflow.WorkflowStartReceiptStore;
import com.grab.framework.workflow.WorkflowFingerprint;
import com.grab.framework.workflow.WorkflowPayloadCodec;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowExecutionStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowHistoryStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowQueryStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowSignalReceiptStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowStartReceiptStore;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowHistoryJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowNodeExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowSignalReceiptJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowStartReceiptJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowTimerJpaRepository;
import com.grab.workflow.infrastructure.service.JpaWorkflowTimerScheduler;
import com.grab.workflow.infrastructure.serialization.JacksonWorkflowFingerprint;
import com.grab.workflow.infrastructure.serialization.JacksonWorkflowPayloadCodec;
import com.grab.workflow.infrastructure.entity.WorkflowStartReceiptEntity;
import com.grab.workflow.infrastructure.entity.WorkflowSignalReceiptEntity;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;

@Configuration
public class WorkflowInfraConfig {

    @Bean
    public WorkflowPayloadCodec workflowPayloadCodec() {
        return new JacksonWorkflowPayloadCodec();
    }

    @Bean
    public WorkflowFingerprint workflowFingerprint() {
        return new JacksonWorkflowFingerprint();
    }



    @Bean
    public WorkflowExecutionStore workflowExecutionStore(
            WorkflowExecutionJpaRepository executionRepository,
            WorkflowNodeExecutionJpaRepository nodeExecutionRepository,
            WorkflowStartReceiptJpaRepository startReceiptRepository,
            WorkflowPayloadCodec payloadCodec
    ) {
        return new JpaWorkflowExecutionStore(executionRepository, nodeExecutionRepository, startReceiptRepository, payloadCodec);
    }

    @Bean
    public WorkflowStartReceiptStore workflowStartReceiptStore(
            WorkflowStartReceiptJpaRepository startReceiptRepository, JpaContext context
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(WorkflowStartReceiptEntity.class);
        return new JpaWorkflowStartReceiptStore(startReceiptRepository, entityManager);
    }

    @Bean
    public WorkflowSignalReceiptStore workflowSignalReceiptStore(
            WorkflowSignalReceiptJpaRepository signalReceiptRepository, JpaContext context
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(WorkflowSignalReceiptEntity.class);
        return new JpaWorkflowSignalReceiptStore(signalReceiptRepository, entityManager);
    }

    @Bean
    public WorkflowHistoryStore workflowHistoryStore(WorkflowHistoryJpaRepository historyRepository) {
        return new JpaWorkflowHistoryStore(historyRepository);
    }

    @Bean
    public WorkflowQueryStore workflowQueryStore(
            WorkflowExecutionJpaRepository executionRepository,
            WorkflowNodeExecutionJpaRepository nodeExecutionRepository,
            WorkflowHistoryJpaRepository historyRepository
    ) {
        return new JpaWorkflowQueryStore(executionRepository, nodeExecutionRepository, historyRepository);
    }

    @Bean
    public JpaWorkflowTimerScheduler workflowTimerScheduler(WorkflowTimerJpaRepository timerRepository) {
        return new JpaWorkflowTimerScheduler(timerRepository);
    }

}
