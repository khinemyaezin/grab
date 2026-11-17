package com.grab.service.sellerbff.internal.adapter.inbound.rest.hateoas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
public class SellerAuthRootController {
    private final boolean oidc;

    public SellerAuthRootController(@Value("${grab.seller-oidc.enabled:false}") boolean oidc) {
        this.oidc = oidc;
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.authRoot().withSelfRel());

        model.add(oidc ? SellerAuthApiLinks.browserLogin() : SellerAuthApiLinks.login());
        model.add(SellerAuthApiLinks.logout());
        if (oidc) {
            model.add(SellerAuthApiLinks.csrf());
        } else {
            model.add(SellerAuthApiLinks.refresh());
        }
        model.add(SellerAuthApiLinks.register());

        model.add(SellerSessionApiLinks.getProfile());
        model.add(SellerSessionApiLinks.listContexts());

        return ResponseEntity.ok(model);
    }
}
