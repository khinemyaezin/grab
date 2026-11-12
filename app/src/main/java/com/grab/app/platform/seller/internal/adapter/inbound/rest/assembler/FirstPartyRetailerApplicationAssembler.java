package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantApplicationController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
                linkTo(methodOn(SellerMerchantApplicationController.class).getFirstPartyApplication(null)).withSelfRel()
        );

        if (entity.completedBasicInfo() && entity.merchantId() != null) {
            model.add(linkTo(methodOn(SellerMerchantController.class)
                    .updateProfile(entity.merchantId(), null, null))
                    .withRel("edit-merchant-application"));
        }
        if (entity.completedBasicInfo()
                && entity.completedContactInfo()
                && entity.completedBusinessRegistration()
                && entity.merchantId() != null) {
            model.add(linkTo(methodOn(SellerMerchantApplicationController.class)
                    .submitApplication(entity.merchantId(), null))
                    .withRel("submit-merchant-application"));
        }

        return model;
    }
}
