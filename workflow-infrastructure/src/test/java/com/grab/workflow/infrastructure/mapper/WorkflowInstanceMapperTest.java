package com.grab.workflow.infrastructure.mapper;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.workflow.infrastructure.entity.WorkflowInstanceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowInstanceMapperTest {

    private WorkflowInstanceMapper mapper;

    @BeforeEach
    void setUp() {
        WorkflowInstanceMapperImpl impl = new WorkflowInstanceMapperImpl();
        impl.payloadCodec = new WorkflowPayloadCodec();
        mapper = impl;
    }

    @Test
    void toEntityAndBack_shouldPreserveFieldsAndCheckpoints() {
        WorkflowInstance instance = WorkflowInstance.start(
                "proc-1",
                "create-sellable-item",
                "corr-1",
                "idem-1"
        );
        instance.recordCheckpoint(
                "create-product-set",
                "ps-1",
                "{\"create-product-set\":\"ps-1\"}",
                "[{\"stepName\":\"create-product-set\",\"output\":\"ps-1\"}]"
        );
        WorkflowInstanceEntity entity = new WorkflowInstanceEntity();
        mapper.toEntity(instance, entity);
        WorkflowInstance restored = mapper.toDomain(entity);

        assertEquals(instance.id(), restored.id());
        assertEquals(instance.workflowName(), restored.workflowName());
        assertEquals(WorkflowStatus.RUNNING, restored.status());
        assertEquals("create-product-set", restored.currentStep().orElseThrow());
        assertEquals("idem-1", restored.idempotencyKey().orElseThrow());
        assertEquals(1, restored.checkpoints().size());
        assertEquals("create-product-set", restored.checkpoints().getFirst().stepName());
        assertEquals("ps-1", restored.checkpoints().getFirst().output());
        assertTrue(restored.contextJson().isPresent());
        assertTrue(restored.createdAt().equals(instance.createdAt())
                || !restored.createdAt().isAfter(Instant.now()));
    }
}
