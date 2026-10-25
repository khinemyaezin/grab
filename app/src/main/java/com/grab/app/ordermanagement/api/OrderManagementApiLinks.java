package com.grab.app.ordermanagement.api;

import org.springframework.hateoas.Link;
import com.grab.app.ordermanagement.cart.CartRootController;
import com.grab.app.ordermanagement.workflows.WorkflowsRootController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class OrderManagementApiLinks {
    private OrderManagementApiLinks() {}

    public static Link cartRoot() {
        return linkTo(methodOn(CartRootController.class).root()).withRel("get-cart-root");
    }
    public static Link workflowsRoot() {
        return linkTo(methodOn(WorkflowsRootController.class).root()).withRel("get-workflows-root");
    }
}
