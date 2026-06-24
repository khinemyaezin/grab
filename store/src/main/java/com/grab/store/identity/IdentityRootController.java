package com.grab.store.identity;

import com.grab.store.identity.internal.api.rest.controller.*;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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

        model.add(linkTo(methodOn(UserAdminController.class)
                .listUsers(null, null))
                .withRel("list-users"));

        model.add(linkTo(methodOn(UserAdminController.class)
                .getUser(null))
                .withRel("get-user"));

        model.add(linkTo(methodOn(RoleAdminController.class).
                listRoles(null, null))
                .withRel("list-roles"));

        // Auth links
        model.add(linkTo(methodOn(AuthController.class)
                .login(null))
                .withRel("login"));

        model.add(linkTo(methodOn(AuthController.class)
                .register(null))
                .withRel("register"));

        return ResponseEntity.ok(model);
    }
}
