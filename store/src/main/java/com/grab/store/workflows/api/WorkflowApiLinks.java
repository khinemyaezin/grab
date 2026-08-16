package com.grab.store.workflows.api;

import com.grab.store.workflows.internal.createsellableproduct.rest.controller.CreateSellableProductController;
import com.grab.store.workflows.internal.updatesellableproduct.rest.controller.UpdateSellableProductController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class WorkflowApiLinks {
    private WorkflowApiLinks(){}

    public static Link createSellableProductLink() {
        return linkTo(methodOn(CreateSellableProductController.class).start(null, null))
                .withRel("create-sellable-product");
    }

    public static Link updateSellableProductLink() {
        return linkTo(methodOn(UpdateSellableProductController.class).start(null, null))
                .withRel("update-sellable-product");
    }
}
