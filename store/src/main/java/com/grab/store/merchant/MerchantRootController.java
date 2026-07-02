package com.grab.store.merchant;

import com.grab.store.merchant.internal.api.rest.controller.C2CApplicationController;
import com.grab.store.merchant.internal.api.rest.controller.FirstPartyRetailerApplicationController;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@MerchantEnabled
@RequestMapping("/api/v1/merchants")
public class MerchantRootController {
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(MerchantRootController.class)
                .root())
                .withSelfRel());

        model.add(linkTo(methodOn(C2CApplicationController.class)
                .getC2CApplication(null))
                .withRel("get-c2c-application"));

        model.add(linkTo(methodOn(C2CApplicationController.class)
                .startC2CApplication(null,null))
                .withRel("create-c2c-application"));

        model.add(linkTo(methodOn(FirstPartyRetailerApplicationController.class)
                .startFirstPartyApplication(null, null))
                .withRel("get-first-party-retailer-application"));

        model.add(linkTo(methodOn(FirstPartyRetailerApplicationController.class)
                .startFirstPartyApplication(null,null))
                .withRel("create-first-party-retailer-application"));
        return ResponseEntity.ok(model);
    }
}
