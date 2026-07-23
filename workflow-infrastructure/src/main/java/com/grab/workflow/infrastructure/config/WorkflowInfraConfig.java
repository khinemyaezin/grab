package com.grab.workflow.infrastructure.config;

import com.grab.workflow.infrastructure.mapper.WorkflowInstanceMapper;
import com.grab.workflow.infrastructure.repository.jpa.JpaWorkflowStore;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowInstanceJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowInfraConfig {
    @Bean
    public JpaWorkflowStore jpaWorkflowStore(WorkflowInstanceJpaRepository workflowInstanceJpaRepository,
                                             WorkflowInstanceMapper mapper) {
        return new JpaWorkflowStore(workflowInstanceJpaRepository, mapper);
    }
}
