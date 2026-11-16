package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerAllocationApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class AllocationModelAssembler implements RepresentationModelAssembler<AllocationResponse, EntityModel<AllocationResponse>> {

    @Override
    public EntityModel<AllocationResponse> toModel(AllocationResponse allocation) {
        if (allocation == null) {
            return null;
        }
        EntityModel<AllocationResponse> model = EntityModel.of(allocation);
        model.add(SellerAllocationApiLinks.deallocateStock());
        model.add(SellerAllocationApiLinks.allocateStock());
        return model;
    }
}
