package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.AllocationController;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocateStockResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocationAvailabilityResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.DeallocateStockResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AllocationModelAssembler {

    public EntityModel<AllocateStockResponse> toAllocateModel(AllocateStockResponse response) {
        EntityModel<AllocateStockResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AllocationController.class).allocate(null, null)).withSelfRel());
        model.add(linkTo(methodOn(AllocationController.class).deallocate(null, null)).withRel("deallocate"));
        model.add(linkTo(methodOn(AllocationController.class)
                .availability(response.sku(), response.requestedQuantity()))
                .withRel("availability"));
        return model;
    }

    public EntityModel<DeallocateStockResponse> toDeallocateModel(DeallocateStockResponse response) {
        EntityModel<DeallocateStockResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AllocationController.class).deallocate(null, null)).withSelfRel());
        model.add(linkTo(methodOn(AllocationController.class).allocate(null, null)).withRel("allocate"));
        model.add(linkTo(methodOn(AllocationController.class)
                .availability(response.sku(), null))
                .withRel("availability"));
        return model;
    }

    public EntityModel<AllocationAvailabilityResponse> toAvailabilityModel(AllocationAvailabilityResponse response) {
        EntityModel<AllocationAvailabilityResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AllocationController.class)
                .availability(response.sku(), response.requestedQuantity() > 0 ? response.requestedQuantity() : null))
                .withSelfRel());
        model.add(linkTo(methodOn(AllocationController.class).allocate(null, null)).withRel("allocate"));
        model.add(linkTo(methodOn(AllocationController.class).deallocate(null, null)).withRel("deallocate"));
        return model;
    }
}
