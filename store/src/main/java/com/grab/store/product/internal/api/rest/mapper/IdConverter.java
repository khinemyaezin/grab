package com.grab.store.product.internal.api.rest.mapper;

import com.grab.framework.id.Id;
import com.grab.store.product.internal.assembler.CommonId;
import org.mapstruct.Mapper;

public class IdConverter {
    public String toId(Id id){
        return id.getValue();
    }

    public Id getId(String id) {
        return new CommonId(id);
    }
}
