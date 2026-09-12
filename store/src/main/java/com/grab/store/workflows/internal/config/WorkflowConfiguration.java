package com.grab.store.workflows.internal.config;

import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.workflow.infrastructure.config.WorkflowInfraConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import(WorkflowInfraConfig.class)
public class WorkflowConfiguration {

    @Bean
    public WorkflowDefinitionRegistry workflowDefinitionRegistry(List<WorkflowProcess<?>> processes) {
        return WorkflowDefinitionRegistry.fromProcesses(processes);
    }
}
