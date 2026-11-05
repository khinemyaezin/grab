package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class AllocationModelAssembler implements RepresentationModelAssembler<AllocationResponse, EntityModel<AllocationResponse>> {

    @Override
    public EntityModel<AllocationResponse> toModel(AllocationResponse allocation) {
        return allocation != null ? EntityModel.of(allocation) : null;
    }
}
