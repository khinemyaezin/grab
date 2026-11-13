package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionCountryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerRegionApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegionModelAssembler implements RepresentationModelAssembler<RegionResponse, EntityModel<RegionResponse>> {

    @Override
    public EntityModel<RegionResponse> toModel(RegionResponse response) {
        if (response == null) {
            return null;
        }
        EntityModel<RegionResponse> model = EntityModel.of(response);
        if (response.regionId() != null) {
            model.add(SellerRegionApiLinks.get(response.regionId()).withSelfRel());
        }
        return model;
    }

    public CollectionModel<EntityModel<RegionCountryResponse>> countriesModel(List<RegionCountryResponse> responses) {
        List<EntityModel<RegionCountryResponse>> items = responses.stream()
                .map(EntityModel::of)
                .toList();
        Link collection = SellerRegionApiLinks.countries().expand();
        CollectionModel<EntityModel<RegionCountryResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        return model;
    }
}
