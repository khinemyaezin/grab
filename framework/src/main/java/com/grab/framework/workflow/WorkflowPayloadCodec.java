package com.grab.framework.workflow;

/**
 * Definition-aware payload serialization boundary. Implementations belong to
 * infrastructure so the workflow engine remains independent of Jackson or any
 * other serialization library.
 */
public interface WorkflowPayloadCodec {

    WorkflowPayloadCodec require(WorkflowDefinition<?, ?> definition, int payloadVersion);

    String writeTyped(Object value);

    <T> T readTyped(String json, Class<T> type);

    <T> T convert(Object value, Class<T> type);
}
