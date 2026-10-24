package com.grab.discovery.adapter.persistence.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection.VariantFact;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;

@Converter
public class DiscoveryVariantFactsConverter implements AttributeConverter<List<VariantFact>, String> {
    private static final TypeReference<List<VariantFact>> FACTS_TYPE = new TypeReference<>() {};
    private static final ObjectMapper MAPPER = createMapper();

    @Override
    public String convertToDatabaseColumn(List<VariantFact> facts) {
        try {
            return MAPPER.writeValueAsString(facts);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize discovery variant facts", exception);
        }
    }

    @Override
    public List<VariantFact> convertToEntityAttribute(String json) {
        if (json == null) {
            return null;
        }
        try {
            List<VariantFact> facts = MAPPER.readValue(json, FACTS_TYPE);
            return List.copyOf(facts);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize discovery variant facts", exception);
        }
    }

    private static ObjectMapper createMapper() {
        SimpleModule ids = new SimpleModule();
        ids.addSerializer(Id.class, new JsonSerializer<>() {
            @Override
            public void serialize(Id id, JsonGenerator generator, SerializerProvider provider) throws IOException {
                generator.writeString(id.getValue());
            }
        });
        ids.addDeserializer(Id.class, new JsonDeserializer<>() {
            @Override
            public Id deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                String value = parser.getValueAsString();
                if (value == null) {
                    throw new IOException("Discovery variant identifier must be a string");
                }
                return new CommonId(value);
            }
        });
        return JsonMapper.builder().addModule(ids).build();
    }
}
