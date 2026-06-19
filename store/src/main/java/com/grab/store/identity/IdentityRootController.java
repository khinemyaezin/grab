package com.grab.store.identity;

import com.grab.store.identity.internal.api.rest.controller.ProfileController;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityRootController {
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();

        model.add(linkTo(methodOn(IdentityRootController.class)
                .root())
                .withSelfRel());

        model.add(linkTo(methodOn(ProfileController.class)
                .profile(null))
                .withRel("get-profile"));

        return ResponseEntity.ok(model);
    }
}
