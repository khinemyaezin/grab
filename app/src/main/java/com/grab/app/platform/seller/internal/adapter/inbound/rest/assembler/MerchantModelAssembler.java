package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantApplicationController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MerchantModelAssembler implements RepresentationModelAssembler<MerchantResponse, EntityModel<MerchantResponse>> {

    @Override
    public EntityModel<MerchantResponse> toModel(MerchantResponse merchant) {
        if (merchant == null) {
            return null;
        }
        EntityModel<MerchantResponse> model = EntityModel.of(merchant);
        if (merchant.merchantId() != null) {
            model.add(linkTo(methodOn(SellerMerchantController.class).getMerchant(merchant.merchantId(), null)).withSelfRel());
            if (isEditableApplication(merchant.status())) {
                model.add(linkTo(methodOn(SellerMerchantController.class)
                        .updateProfile(merchant.merchantId(), null, null))
                        .withRel("edit-merchant-application"));
                model.add(linkTo(methodOn(SellerMerchantApplicationController.class)
                        .submitApplication(merchant.merchantId(), null))
                        .withRel("submit-merchant-application"));
            }
        }
        model.add(linkTo(methodOn(SellerMerchantController.class).listMyMerchants(null)).withRel("list-my-merchants"));
        return model;
    }

    private boolean isEditableApplication(String status) {
        if (status == null) {
            return false;
        }
        return "DRAFT".equalsIgnoreCase(status) || "CHANGES_REQUESTED".equalsIgnoreCase(status);
    }
}
