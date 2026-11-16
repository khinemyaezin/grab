package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantTypeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerVariantOptionApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VariantTypeModelAssembler implements RepresentationModelAssembler<VariantTypeResponse, EntityModel<VariantTypeResponse>> {

    @Override
    public EntityModel<VariantTypeResponse> toModel(VariantTypeResponse type) {
        if (type == null) {
            return null;
        }
        EntityModel<VariantTypeResponse> model = EntityModel.of(type);
        if (type.id() != null) {
            model.add(SellerVariantOptionApiLinks.listVariantOptions(null, type.id()).expand());
        }
        return model;
    }

    public CollectionModel<EntityModel<VariantTypeResponse>> listVariantTypesModel(List<VariantTypeResponse> responses, String name) {
        List<EntityModel<VariantTypeResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerVariantOptionApiLinks.listVariantTypes(name).expand();
        CollectionModel<EntityModel<VariantTypeResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        return model;
    }
}
