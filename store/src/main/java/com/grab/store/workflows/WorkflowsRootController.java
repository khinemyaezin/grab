package com.grab.store.workflows;

import com.grab.store.workflows.internal.createsellableproduct.rest.controller.CreateSellableProductController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowsRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(WorkflowsRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(CreateSellableProductController.class).start(null, null))
                .withRel("create-sellable-product"));
        model.add(linkTo(methodOn(CreateSellableProductController.class).get(null, null))
                .withRel("get-create-sellable-product"));
        return ResponseEntity.ok(model);
    }
}
