package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerPricingApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PriceSetModelAssembler implements RepresentationModelAssembler<PriceSetResponse, EntityModel<PriceSetResponse>> {

    @Override
    public EntityModel<PriceSetResponse> toModel(PriceSetResponse priceSet) {
        return assemble(priceSet, true);
    }

    public EntityModel<PriceSetResponse> toCommandModel(PriceSetResponse priceSet) {
        return assemble(priceSet, false);
    }

    private EntityModel<PriceSetResponse> assemble(PriceSetResponse priceSet, boolean canonicalRead) {
        if (priceSet == null) {
            return null;
        }
        EntityModel<PriceSetResponse> model = EntityModel.of(priceSet);
        if (priceSet.id() != null) {
            Link retrieval = SellerPricingApiLinks.getPriceSet(priceSet.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
        }
        model.add(SellerPricingApiLinks.calculatePrices());
        return model;
    }
}
