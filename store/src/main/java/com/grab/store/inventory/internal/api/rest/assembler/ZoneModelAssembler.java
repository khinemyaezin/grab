package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ZoneModelAssembler implements RepresentationModelAssembler<ZoneResponse, EntityModel<ZoneResponse>> {

    @Override
    public EntityModel<ZoneResponse> toModel(ZoneResponse response) {
        return EntityModel.of(response);
    }
}
