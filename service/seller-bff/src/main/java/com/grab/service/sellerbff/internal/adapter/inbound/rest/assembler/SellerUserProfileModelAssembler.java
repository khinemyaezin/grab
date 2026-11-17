package com.grab.service.sellerbff.internal.adapter.inbound.rest.assembler;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.hateoas.SellerAuthApiLinks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SellerUserProfileModelAssembler
        implements RepresentationModelAssembler<SellerUserProfileResponse, EntityModel<SellerUserProfileResponse>> {

    private final boolean oidc;

    public SellerUserProfileModelAssembler(@Value("${grab.seller-oidc.enabled:false}") boolean oidc) {
        this.oidc = oidc;
    }

    @Override
    public EntityModel<SellerUserProfileResponse> toModel(SellerUserProfileResponse response) {
        if (response == null) {
            return null;
        }
        return EntityModel.of(
                response,
                oidc ? SellerAuthApiLinks.browserLogin() : SellerAuthApiLinks.login()
        );
    }
}

