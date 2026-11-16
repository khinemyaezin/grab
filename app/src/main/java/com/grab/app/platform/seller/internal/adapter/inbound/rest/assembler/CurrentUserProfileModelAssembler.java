package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CurrentUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerSessionApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProfileModelAssembler
        implements RepresentationModelAssembler<CurrentUserProfileResponse, EntityModel<CurrentUserProfileResponse>> {

    @Override
    public EntityModel<CurrentUserProfileResponse> toModel(CurrentUserProfileResponse response) {
        if (response == null) {
            return null;
        }
        EntityModel<CurrentUserProfileResponse> model = EntityModel.of(response);
        model.add(SellerSessionApiLinks.getProfile().withSelfRel());
        model.add(SellerSessionApiLinks.listContexts());
        return model;
    }
}
