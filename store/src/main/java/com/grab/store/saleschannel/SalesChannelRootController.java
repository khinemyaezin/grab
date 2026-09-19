package com.grab.store.saleschannel;

import com.grab.store.saleschannel.internal.api.rest.controller.SalesChannelController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/sales-channels")
public class SalesChannelRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SalesChannelRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(SalesChannelController.class).list(null, null))
                .withRel("list-sales-channels"));
        model.add(linkTo(methodOn(SalesChannelController.class).get(null))
                .withRel("get-sales-channel"));
        return ResponseEntity.ok(model);
    }
}
