package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSellableProductWorkflowController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerWorkflowApiLinks {

    private SellerWorkflowApiLinks() {
    }

    public static Link getUpdateProductVariant(String workflowId) {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateProductVariant(workflowId))
                .withRel("get-update-product-variant");
    }

    public static Link startUpdateProductVariant() {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateProductVariant(null, null))
                .withRel("update-product-variant");
    }

    public static Link getCreateSellableProduct(String workflowId) {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).getCreateSellableProduct(workflowId))
                .withRel("get-create-sellable-product");
    }

    public static Link startCreateSellableProduct() {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).startCreateSellableProduct(null, null))
                .withRel("create-sellable-product");
    }

    public static Link getWorkflowStatus(String workflowId) {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).getWorkflowStatus(workflowId))
                .withRel("get-workflow-status");
    }

    public static Link listRecentWorkflows(int page, int size) {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).listRecentWorkflows(page, size))
                .withRel("list-recent-workflows");
    }

    public static Link getUpdateSellableProduct(String workflowId) {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateSellableProduct(workflowId))
                .withRel("get-update-sellable-product");
    }

    public static Link startUpdateSellableProduct() {
        return linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateSellableProduct(null, null))
                .withRel("update-sellable-product");
    }
}
