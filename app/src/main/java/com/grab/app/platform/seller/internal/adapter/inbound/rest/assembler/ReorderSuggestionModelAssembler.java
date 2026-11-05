package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ReorderSuggestionModelAssembler implements RepresentationModelAssembler<ReorderSuggestionResponse, EntityModel<ReorderSuggestionResponse>> {

    @Override
    public EntityModel<ReorderSuggestionResponse> toModel(ReorderSuggestionResponse suggestion) {
        return suggestion != null ? EntityModel.of(suggestion) : null;
    }
}
