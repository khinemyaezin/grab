package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantOptionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerVariantOptionApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VariantOptionModelAssembler implements RepresentationModelAssembler<VariantOptionResponse, EntityModel<VariantOptionResponse>> {

    @Override
    public EntityModel<VariantOptionResponse> toModel(VariantOptionResponse option) {
        if (option == null) {
            return null;
        }
        EntityModel<VariantOptionResponse> model = EntityModel.of(option);
        if (option.typeId() != null) {
            model.add(SellerVariantOptionApiLinks.listVariantOptions(null, option.typeId()).expand());
        }
        return model;
    }

    public CollectionModel<EntityModel<VariantOptionResponse>> listVariantOptionsModel(List<VariantOptionResponse> responses, String name, String typeId) {
        List<EntityModel<VariantOptionResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerVariantOptionApiLinks.listVariantOptions(name, typeId).expand();
        CollectionModel<EntityModel<VariantOptionResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        return model;
    }
}
