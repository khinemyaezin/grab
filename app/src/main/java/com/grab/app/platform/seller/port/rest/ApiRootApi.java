package com.grab.app.platform.seller.port.rest;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/seller")
public interface ApiRootApi {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<RepresentationModel<?>> root();
}
