package com.grab.service.sellerbff.internal.adapter.inbound.rest.hateoas;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
public class SellerAuthRootController {
    private final SellerAuthApiLinks authLinks;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.authRoot().withSelfRel());

        model.add(authLinks.browserLogin());
        model.add(authLinks.logout());
        model.add(authLinks.csrf());
        model.add(authLinks.register());

        model.add(SellerSessionApiLinks.getProfile());
        model.add(SellerSessionApiLinks.listContexts());

        return ResponseEntity.ok(model);
    }
}
