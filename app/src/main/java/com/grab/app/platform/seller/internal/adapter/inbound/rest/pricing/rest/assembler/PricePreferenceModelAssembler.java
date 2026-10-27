package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.PricePreferenceController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.PricePreferenceResponse;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@PricingEnabled
public class PricePreferenceModelAssembler
        implements RepresentationModelAssembler<PricePreferenceResponse, EntityModel<PricePreferenceResponse>> {

    @Override
    public EntityModel<PricePreferenceResponse> toModel(PricePreferenceResponse response) {
        String id = response.id();
        EntityModel<PricePreferenceResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PricePreferenceController.class).getPreference(id)).withSelfRel());
        model.add(linkTo(methodOn(PricePreferenceController.class).updatePreference(id, null))
                .withRel("update-price-preference"));
        model.add(linkTo(methodOn(PricePreferenceController.class).deletePreference(id))
                .withRel("delete-price-preference"));
        model.add(linkTo(methodOn(PricePreferenceController.class).listPreferences())
                .withRel("list-price-preferences"));
        return model;
    }
}
