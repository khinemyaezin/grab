package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerStorefrontController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StorefrontModelAssembler implements RepresentationModelAssembler<StorefrontResponse, EntityModel<StorefrontResponse>> {

    @Override
    public EntityModel<StorefrontResponse> toModel(StorefrontResponse storefront) {
        if (storefront == null) {
            return null;
        }
        EntityModel<StorefrontResponse> model = EntityModel.of(storefront);
        if (storefront.merchantId() != null && storefront.storefrontId() != null) {
            String merchantId = storefront.merchantId();
            String storefrontId = storefront.storefrontId();
            model.add(linkTo(methodOn(SellerStorefrontController.class).getStorefront(merchantId, storefrontId)).withSelfRel());
            model.add(linkTo(methodOn(SellerStorefrontController.class).listStorefronts(merchantId)).withRel("list-storefronts"));
            model.add(linkTo(methodOn(SellerStorefrontController.class).createStorefront(merchantId, null)).withRel("create-storefront"));

            String status = storefront.status();
            if (!"CLOSED".equalsIgnoreCase(status)) {
                model.add(linkTo(methodOn(SellerStorefrontController.class).updateStorefront(merchantId, storefrontId, null)).withRel("update-storefront"));
                model.add(linkTo(methodOn(SellerStorefrontController.class).closeStorefront(merchantId, storefrontId)).withRel("close-storefront"));
            }
            if ("DRAFT".equalsIgnoreCase(status)) {
                model.add(linkTo(methodOn(SellerStorefrontController.class).activateStorefront(merchantId, storefrontId)).withRel("activate-storefront"));
            }
        }
        return model;
    }
}
