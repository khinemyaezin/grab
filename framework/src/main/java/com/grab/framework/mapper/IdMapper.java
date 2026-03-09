package com.grab.framework.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;

import java.util.Objects;

public class IdMapper {
    private final IdGenerator idGenerator;

    public IdMapper(IdGenerator idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public String map(Id id) {
        return id != null ? id.getValue() : null;
    }

    public Id map(String id) {
        return id == null || id.isBlank() ? null : idGenerator.generateId(id);
    }
}
