package com.grab.service.sellerbff.internal.adapter.inbound.rest.assembler;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.hateoas.SellerAuthApiLinks;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerUserProfileModelAssembler
        implements RepresentationModelAssembler<SellerUserProfileResponse, EntityModel<SellerUserProfileResponse>> {
    private final SellerAuthApiLinks authLinks;

    @Override
    public EntityModel<SellerUserProfileResponse> toModel(SellerUserProfileResponse response) {
        return EntityModel.of(
                response,
                authLinks.browserLogin()
        );
    }
}

