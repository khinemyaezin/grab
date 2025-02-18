package com.grab.store.product.internal.usecase;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.grab.store.product.internal.api.rest.dto.CreatedResponse;

public abstract class AbstractPersitUseCase<REQ, RESP extends Entity<Id>> {
    public CreatedResponse perform(REQ req) {
        var response = handle(req);
        return CreatedResponse.builder()
                .id(response.getId().get())
                .build();
    }

    public abstract RESP handle(REQ req);

}
