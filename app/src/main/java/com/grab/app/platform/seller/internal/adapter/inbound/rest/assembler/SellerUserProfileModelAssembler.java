package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerAuthController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SellerUserProfileModelAssembler
        implements RepresentationModelAssembler<SellerUserProfileResponse, EntityModel<SellerUserProfileResponse>> {

    @Override
    public EntityModel<SellerUserProfileResponse> toModel(SellerUserProfileResponse response) {
        if (response == null) {
            return null;
        }
        return EntityModel.of(
                response,
                linkTo(methodOn(SellerAuthController.class).register(null)).withSelfRel(),
                linkTo(methodOn(SellerAuthController.class).login(null)).withRel("login")
        );
    }
}

