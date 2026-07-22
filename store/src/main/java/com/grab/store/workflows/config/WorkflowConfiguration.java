package com.grab.store.workflows.config;

import com.grab.framework.workflow.WorkflowRunner;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.impl.DefaultWorkflowRunner;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowConfiguration {

    @Bean
    public WorkflowPayloadCodec workflowPayloadCodec() {
        return new WorkflowPayloadCodec();
    }

    @Bean
    public WorkflowRunner workflowRunner(
            WorkflowStore workflowStore,
            WorkflowPayloadCodec workflowPayloadCodec
    ) {
        return new DefaultWorkflowRunner(workflowStore, workflowPayloadCodec);
    }
}
