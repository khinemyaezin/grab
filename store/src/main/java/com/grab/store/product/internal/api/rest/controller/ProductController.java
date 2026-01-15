package com.grab.store.product.internal.api.rest.controller;

import com.grab.store.product.internal.api.rest.dto.request.BuildProductRequest;
import com.grab.store.product.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.product.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.product.internal.api.rest.dto.response.BuildProductResponse;
import com.grab.store.product.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.product.internal.api.rest.service.ProductFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFacadeService productFacadeService;

    @PostMapping(
            value = "/combinations",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<ProductCombinationResponse>> generateCombinations(
            @Valid @RequestBody ProductCombinationRequest request) {

        EntityModel<ProductCombinationResponse> response =
                productFacadeService.generateCombinations(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/build",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<BuildProductResponse>> buildProduct(
            @Valid @RequestBody BuildProductRequest request) {

        EntityModel<BuildProductResponse> response =
                productFacadeService.buildProduct(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> saveProduct(
            @Valid @RequestBody SaveProductRequest request) {

        String productId = productFacadeService.saveProduct(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productId)
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
