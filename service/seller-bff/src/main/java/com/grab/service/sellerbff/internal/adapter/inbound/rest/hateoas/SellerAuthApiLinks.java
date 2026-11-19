package com.grab.service.sellerbff.internal.adapter.inbound.rest.hateoas;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.controller.SellerOidcAuthController;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@RequiredArgsConstructor
public final class SellerAuthApiLinks {
    private final SellerOidcProperties oidc;

    public Link browserLogin() {
        String url = oidc.browserLoginUrl();
        return Link.of(url, "login");
    }

    public Link csrf() {
        return linkTo(methodOn(SellerOidcAuthController.class).csrf(null)).withRel("get-csrf-token");
    }

    public Link register() {
        return linkTo(methodOn(SellerOidcAuthController.class).register())
                .withRel("register");
    }
    public Link logout() {
        return linkTo(methodOn(SellerOidcAuthController.class).logout(null, null))
                .withRel("logout");
    }
}
