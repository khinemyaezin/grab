package com.grab.store.product.internal.api.rest.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdConverter {
    private IdGenerator idGenerator;

    public String toId(Id id){
        return id == null ? null : id.getValue();
    }

    public Id getId(String id) {
        return idGenerator.generateId(id);
    }
}
