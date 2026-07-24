package com.grab.store.pricing.internal.api.rest.assembler;

import com.grab.store.pricing.internal.api.rest.controller.PriceListController;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.grab.store.pricing.internal.config.PricingEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@PricingEnabled
public class PriceListModelAssembler
        implements RepresentationModelAssembler<PriceListResponse, EntityModel<PriceListResponse>> {

    @Override
    public EntityModel<PriceListResponse> toModel(PriceListResponse response) {
        String id = response.id();
        EntityModel<PriceListResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PriceListController.class).getPriceList(id)).withSelfRel());
        model.add(linkTo(methodOn(PriceListController.class).updatePriceList(id, null)).withRel("update-price-list"));
        model.add(linkTo(methodOn(PriceListController.class).replaceRules(id, null)).withRel("replace-price-list-rules"));
        model.add(linkTo(methodOn(PriceListController.class).addPrice(id, null)).withRel("add-price-list-price"));
        model.add(linkTo(methodOn(PriceListController.class).deletePriceList(id)).withRel("delete-price-list"));
        model.add(linkTo(methodOn(PriceListController.class).listPriceLists()).withRel("list-price-lists"));
        return model;
    }
}
