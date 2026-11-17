package com.grab.service.sellerbff.internal.adapter.inbound.rest.hateoas;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.controller.SellerAuthController;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.controller.SellerOidcAuthController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerAuthApiLinks {

    private SellerAuthApiLinks() {
    }

    public static Link browserLogin() {
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/oauth2/authorization/seller").toUriString();
        return Link.of(url, "login");
    }

    public static Link csrf() {
        return linkTo(methodOn(SellerOidcAuthController.class).csrf(null)).withRel("get-csrf-token");
    }

    public static Link login() {
        return linkTo(methodOn(SellerAuthController.class).login(null))
                .withRel("login");
    }

    public static Link register() {
        return linkTo(methodOn(SellerAuthController.class).register(null))
                .withRel("register");
    }

    public static Link refresh() {
        return linkTo(methodOn(SellerAuthController.class).refresh(null, null))
                .withRel("refresh");
    }

    public static Link logout() {
        return linkTo(methodOn(SellerAuthController.class).logout(null, null))
                .withRel("logout");
    }
}
