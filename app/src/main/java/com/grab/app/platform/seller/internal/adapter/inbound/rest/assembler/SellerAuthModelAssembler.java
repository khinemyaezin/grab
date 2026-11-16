package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerAuthApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

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
                SellerAuthApiLinks.refresh(),
                SellerAuthApiLinks.logout()
        );
    }
}

