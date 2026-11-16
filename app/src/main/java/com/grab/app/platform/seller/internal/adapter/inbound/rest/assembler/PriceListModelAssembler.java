package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerPricingApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceListModelAssembler implements RepresentationModelAssembler<PriceListResponse, EntityModel<PriceListResponse>> {

    @Override
    public EntityModel<PriceListResponse> toModel(PriceListResponse priceList) {
        return assemble(priceList, true);
    }

    public EntityModel<PriceListResponse> toCommandModel(PriceListResponse priceList) {
        return assemble(priceList, false);
    }

    private EntityModel<PriceListResponse> assemble(PriceListResponse priceList, boolean canonicalRead) {
        if (priceList == null) {
            return null;
        }
        EntityModel<PriceListResponse> model = EntityModel.of(priceList);
        if (priceList.id() != null) {
            Link retrieval = SellerPricingApiLinks.getPriceList(priceList.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
        }
        model.add(SellerPricingApiLinks.listPriceLists());
        return model;
    }

    public CollectionModel<EntityModel<PriceListResponse>> listPriceListsModel(List<PriceListResponse> responses) {
        List<EntityModel<PriceListResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerPricingApiLinks.listPriceLists().expand();
        CollectionModel<EntityModel<PriceListResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        model.add(SellerPricingApiLinks.createPriceList());
        return model;
    }
}
