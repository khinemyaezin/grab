package com.grab.store.product.internal.api.rest.mapper;

import com.grab.framework.id.Id;
import com.grab.store.product.internal.assembler.CommonId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;

@Component
@AllArgsConstructor
public class CreateCommandMapper {
    public Id createId() {
        return new CommonId();
    }
}
