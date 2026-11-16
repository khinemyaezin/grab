package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerPricingApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PricePreferenceModelAssembler implements RepresentationModelAssembler<PricePreferenceResponse, EntityModel<PricePreferenceResponse>> {

    @Override
    public EntityModel<PricePreferenceResponse> toModel(PricePreferenceResponse preference) {
        return assemble(preference, true);
    }

    public EntityModel<PricePreferenceResponse> toCommandModel(PricePreferenceResponse preference) {
        return assemble(preference, false);
    }

    private EntityModel<PricePreferenceResponse> assemble(PricePreferenceResponse preference, boolean canonicalRead) {
        if (preference == null) {
            return null;
        }
        EntityModel<PricePreferenceResponse> model = EntityModel.of(preference);
        if (preference.id() != null) {
            Link retrieval = SellerPricingApiLinks.getPreference(preference.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
        }
        return model;
    }
}
