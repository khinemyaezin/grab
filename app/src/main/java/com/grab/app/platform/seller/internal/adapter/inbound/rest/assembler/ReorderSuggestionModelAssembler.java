package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerReorderSuggestionApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReorderSuggestionModelAssembler implements RepresentationModelAssembler<ReorderSuggestionResponse, EntityModel<ReorderSuggestionResponse>> {

    @Override
    public EntityModel<ReorderSuggestionResponse> toModel(ReorderSuggestionResponse suggestion) {
        return suggestion != null ? EntityModel.of(suggestion) : null;
    }

    public CollectionModel<EntityModel<ReorderSuggestionResponse>> listSuggestionsModel(List<ReorderSuggestionResponse> responses, String locationId, String sku) {
        List<EntityModel<ReorderSuggestionResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerReorderSuggestionApiLinks.listSuggestions(locationId, sku).expand();
        CollectionModel<EntityModel<ReorderSuggestionResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        return model;
    }
}
