package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerMerchantApiLinks;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerMerchantApplicationApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class FirstPartyRetailerApplicationAssembler
        implements RepresentationModelAssembler<GetFirstPartyRetailerApplicationResponse, EntityModel<GetFirstPartyRetailerApplicationResponse>> {

    @Override
    public EntityModel<GetFirstPartyRetailerApplicationResponse> toModel(GetFirstPartyRetailerApplicationResponse entity) {
        if (entity == null) {
            return null;
        }

        EntityModel<GetFirstPartyRetailerApplicationResponse> model = EntityModel.of(
                entity,
                SellerMerchantApplicationApiLinks.getFirstPartyApplication().withSelfRel()
        );

        if (entity.completedBasicInfo() && entity.merchantId() != null) {
            model.add(SellerMerchantApiLinks.updateProfile(entity.merchantId()));
        }
        if (entity.completedBasicInfo()
                && entity.completedContactInfo()
                && entity.completedBusinessRegistration()
                && entity.merchantId() != null) {
            model.add(SellerMerchantApplicationApiLinks.submitApplication(entity.merchantId()));
        }

        return model;
    }
}
