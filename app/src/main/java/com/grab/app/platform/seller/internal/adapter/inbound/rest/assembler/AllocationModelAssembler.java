package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerAllocationController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AllocationModelAssembler implements RepresentationModelAssembler<AllocationResponse, EntityModel<AllocationResponse>> {

    @Override
    public EntityModel<AllocationResponse> toModel(AllocationResponse allocation) {
        if (allocation == null) {
            return null;
        }
        EntityModel<AllocationResponse> model = EntityModel.of(allocation);
        model.add(linkTo(methodOn(SellerAllocationController.class).allocateStock(null, null)).withSelfRel());
        model.add(linkTo(methodOn(SellerAllocationController.class).deallocateStock(null, null)).withRel("deallocate"));
        model.add(linkTo(methodOn(SellerAllocationController.class).allocateStock(null, null)).withRel("allocate"));
        return model;
    }
}
