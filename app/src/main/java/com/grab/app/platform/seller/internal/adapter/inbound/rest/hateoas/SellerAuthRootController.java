package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
public class SellerAuthRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.authRoot().withSelfRel());

        model.add(SellerAuthApiLinks.login());
        model.add(SellerAuthApiLinks.logout());
        model.add(SellerAuthApiLinks.refresh());
        model.add(SellerAuthApiLinks.register());

        model.add(SellerSessionApiLinks.getProfile());
        model.add(SellerSessionApiLinks.listContexts());

        return ResponseEntity.ok(model);
    }
}
