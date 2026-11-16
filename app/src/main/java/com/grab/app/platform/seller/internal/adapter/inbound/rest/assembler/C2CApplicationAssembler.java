package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetC2CApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerMerchantApiLinks;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerMerchantApplicationApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class C2CApplicationAssembler
        implements RepresentationModelAssembler<GetC2CApplicationResponse, EntityModel<GetC2CApplicationResponse>> {

    @Override
    public EntityModel<GetC2CApplicationResponse> toModel(GetC2CApplicationResponse entity) {

        EntityModel<GetC2CApplicationResponse> model = EntityModel.of(
                entity,
                SellerMerchantApplicationApiLinks.getC2CApplication().withSelfRel()
        );

        if (entity.completedBasicInfo() && entity.merchantId() != null) {
            model.add(SellerMerchantApiLinks.updateProfile(entity.merchantId()));
        }
        if (entity.completedBasicInfo() && entity.completedContactInfo() && entity.merchantId() != null) {
            model.add(SellerMerchantApplicationApiLinks.submitApplication(entity.merchantId()));
        }

        return model;
    }
}
