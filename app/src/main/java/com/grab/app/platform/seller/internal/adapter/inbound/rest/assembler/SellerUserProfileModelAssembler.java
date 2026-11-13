package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerAuthApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

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
                SellerAuthApiLinks.login()
        );
    }
}

