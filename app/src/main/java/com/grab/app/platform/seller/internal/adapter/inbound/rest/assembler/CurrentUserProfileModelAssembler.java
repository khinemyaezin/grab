package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSessionController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CurrentUserProfileResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CurrentUserProfileModelAssembler
        implements RepresentationModelAssembler<CurrentUserProfileResponse, EntityModel<CurrentUserProfileResponse>> {

    @Override
    public EntityModel<CurrentUserProfileResponse> toModel(CurrentUserProfileResponse response) {
        if (response == null) {
            return null;
        }
        EntityModel<CurrentUserProfileResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(SellerSessionController.class).getProfile(null)).withSelfRel());
        model.add(linkTo(methodOn(SellerSessionController.class).listContexts(null)).withRel("list-access-contexts"));
        return model;
    }
}
