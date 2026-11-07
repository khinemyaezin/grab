package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPriceListController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PriceListModelAssembler implements RepresentationModelAssembler<PriceListResponse, EntityModel<PriceListResponse>> {

    @Override
    public EntityModel<PriceListResponse> toModel(PriceListResponse priceList) {
        if (priceList == null) {
            return null;
        }
        EntityModel<PriceListResponse> model = EntityModel.of(priceList);
        if (priceList.id() != null) {
            model.add(linkTo(methodOn(SellerPriceListController.class).getPriceList(priceList.id())).withSelfRel());
        }
        model.add(linkTo(methodOn(SellerPriceListController.class).listPriceLists()).withRel("list-price-lists"));
        return model;
    }
}
