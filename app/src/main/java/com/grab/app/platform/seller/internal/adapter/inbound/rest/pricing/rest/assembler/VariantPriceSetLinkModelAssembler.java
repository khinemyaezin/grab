package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.PriceSetController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.VariantPriceSetLinkResponse;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@PricingEnabled
public class VariantPriceSetLinkModelAssembler
        implements RepresentationModelAssembler<VariantPriceSetLinkResponse, EntityModel<VariantPriceSetLinkResponse>> {

    @Override
    public EntityModel<VariantPriceSetLinkResponse> toModel(VariantPriceSetLinkResponse response) {
        EntityModel<VariantPriceSetLinkResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PriceSetController.class).getPriceSet(response.priceSetId()))
                .withRel("get-price-set"));
        model.add(linkTo(methodOn(PriceSetController.class).calculatePrices(null))
                .withRel("calculate-prices"));
        return model;
    }
}
