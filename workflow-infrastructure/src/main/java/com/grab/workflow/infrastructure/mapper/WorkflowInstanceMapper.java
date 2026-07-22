package com.grab.workflow.infrastructure.mapper;

import com.grab.framework.workflow.WorkflowCheckpoint;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.workflow.infrastructure.entity.WorkflowInstanceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class WorkflowInstanceMapper {

    @Autowired
    protected WorkflowPayloadCodec payloadCodec;

    @Mapping(target = "id", expression = "java(instance.id())")
    @Mapping(target = "workflowName", expression = "java(instance.workflowName())")
    @Mapping(target = "status", expression = "java(instance.status())")
    @Mapping(target = "currentStep", expression = "java(instance.currentStep().orElse(null))")
    @Mapping(target = "correlationId", expression = "java(instance.correlationId())")
    @Mapping(target = "idempotencyKey", expression = "java(instance.idempotencyKey().orElse(null))")
    @Mapping(target = "contextJson", expression = "java(instance.contextJson().orElse(null))")
    @Mapping(target = "checkpointJson", expression = "java(instance.checkpointJson().orElse(null))")
    @Mapping(target = "errorMessage", expression = "java(instance.errorMessage().orElse(null))")
    @Mapping(target = "createdAt", expression = "java(instance.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(instance.updatedAt())")
    public abstract void toEntity(WorkflowInstance instance, @MappingTarget WorkflowInstanceEntity entity);

    public WorkflowInstance toDomain(WorkflowInstanceEntity entity) {
        List<WorkflowCheckpoint> checkpoints = payloadCodec.readCheckpoints(entity.getCheckpointJson());
        return WorkflowInstance.restore(
                entity.getId(),
                entity.getWorkflowName(),
                entity.getCorrelationId(),
                entity.getIdempotencyKey(),
                entity.getStatus(),
                entity.getCurrentStep(),
                entity.getContextJson(),
                entity.getCheckpointJson(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                checkpoints
        );
    }
}
