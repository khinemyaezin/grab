package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerAuthController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SellerAuthModelAssembler
        implements RepresentationModelAssembler<SellerAuthResponse, EntityModel<SellerAuthResponse>> {

    @Override
    public EntityModel<SellerAuthResponse> toModel(SellerAuthResponse response) {
        if (response == null) {
            return null;
        }
        return EntityModel.of(
                response,
                linkTo(methodOn(SellerAuthController.class).login(null)).withSelfRel(),
                linkTo(methodOn(SellerAuthController.class).refresh(null, null)).withRel("refresh-token"),
                linkTo(methodOn(SellerAuthController.class).logout(null, null)).withRel("logout")
        );
    }
}

