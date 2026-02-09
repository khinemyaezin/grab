package com.grab.store.product.internal.api.rest.controller;

import com.grab.store.product.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.product.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.product.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.product.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.product.internal.api.rest.dto.response.DeleteProductResponse;
import com.grab.store.product.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.product.internal.api.rest.service.ProductFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFacadeService productFacadeService;

    @PostMapping(
            value = "/search",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<ProductSummaryResponse>> getProducts(@Valid @RequestBody ProductSummaryRequest request) {
        EntityModel<ProductSummaryResponse> response = productFacadeService.getProductSummary(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/combination",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<ProductCombinationResponse>> getProductVariationCombination(
            @Valid @RequestBody ProductCombinationRequest request) {

        EntityModel<ProductCombinationResponse> response =
                productFacadeService.getProductCombination(request);

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

    @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<DeleteProductResponse>> deleteProduct(@PathVariable("productId") String productId) {
        EntityModel<DeleteProductResponse> response = productFacadeService.deleteProduct(productId);

        if (response.getContent() != null && response.getContent().deleted()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}
