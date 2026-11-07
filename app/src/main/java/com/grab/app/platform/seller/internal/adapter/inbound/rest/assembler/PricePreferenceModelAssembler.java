package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPricePreferenceController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PricePreferenceModelAssembler implements RepresentationModelAssembler<PricePreferenceResponse, EntityModel<PricePreferenceResponse>> {

    @Override
    public EntityModel<PricePreferenceResponse> toModel(PricePreferenceResponse preference) {
        if (preference == null) {
            return null;
        }
        EntityModel<PricePreferenceResponse> model = EntityModel.of(preference);
        if (preference.id() != null) {
            model.add(linkTo(methodOn(SellerPricePreferenceController.class).getPreference(preference.id())).withSelfRel());
        }
        return model;
    }
}
