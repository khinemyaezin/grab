package com.grab.workflow.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;

import java.io.IOException;

public final class WorkflowPayloadJacksonModule extends SimpleModule {

    public WorkflowPayloadJacksonModule() {
        addSerializer(Id.class, new WorkflowIdSerializer());
        addDeserializer(Id.class, new WorkflowIdDeserializer());
    }

    private static final class WorkflowIdSerializer extends JsonSerializer<Id> {

        @Override
        public void serialize(Id value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            generator.writeString(value.getValue());
        }
    }

    private static final class WorkflowIdDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<Id> {

        @Override
        public Id deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return new CommonId(parser.getValueAsString());
        }
    }
}
