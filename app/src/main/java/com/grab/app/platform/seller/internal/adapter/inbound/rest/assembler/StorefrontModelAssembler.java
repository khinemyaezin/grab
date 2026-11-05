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
            model.add(linkTo(methodOn(SellerStorefrontController.class).getStorefront(storefront.merchantId(), storefront.storefrontId())).withSelfRel());
            model.add(linkTo(methodOn(SellerStorefrontController.class).listStorefronts(storefront.merchantId())).withRel("list-storefronts"));
        }
        return model;
    }
}
