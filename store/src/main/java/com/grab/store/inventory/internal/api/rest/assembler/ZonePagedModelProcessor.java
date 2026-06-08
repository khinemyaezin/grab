package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.ZoneController;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ZonePagedModelProcessor implements RepresentationModelProcessor<PagedModel<EntityModel<ZoneResponse>>> {

    @Override
    public PagedModel<EntityModel<ZoneResponse>> process(PagedModel<EntityModel<ZoneResponse>> model) {
        extractLocationId(model).ifPresent(locationId ->
                model.add(linkTo(methodOn(ZoneController.class)
                        .createZone(locationId, null, null)).withRel("create"))
        );
        return model;
    }

    private Optional<String> extractLocationId(PagedModel<EntityModel<ZoneResponse>> model) {
        return model.getContent().stream()
                .findFirst()
                .map(EntityModel::getContent)
                .map(ZoneResponse::locationId);
    }
}
