package com.grab.store.product.internal.api.rest.controller;

import com.grab.store.product.internal.api.rest.dto.CreateProductRequest;
import com.grab.store.product.internal.api.rest.dto.ProductDto;
import com.grab.store.product.internal.api.rest.dto.UpdateProductRequest;
import com.grab.store.product.internal.api.rest.service.ProductFacadeService;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {
    private final ProductFacadeService productFacadeService;

    public ProductController(ProductFacadeService productFacadeService) {
        this.productFacadeService = productFacadeService;
    }

    @PostMapping(value = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductDto>> createProduct(@RequestBody CreateProductRequest persistableProduct) {
        ProductDto productDTO = productFacadeService.createProduct(persistableProduct);

        EntityModel<ProductDto> resource = EntityModel.of(productDTO);
        resource.add(linkTo(methodOn(ProductController.class).createProduct(persistableProduct)).withSelfRel());
        resource.add(linkTo(methodOn(ProductController.class).getProduct(productDTO.id())).withRel("get-product"));
        resource.add(linkTo(methodOn(ProductController.class).updateProduct(null)).withRel("update-product"));

        return ResponseEntity
                .created(linkTo(methodOn(ProductController.class).getProduct(productDTO.id())).withRel("get-product").toUri())
                .body(resource);
    }

    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductDto>> updateProduct(@RequestBody UpdateProductRequest updateProductRequest) {
        ProductDto productDTO = productFacadeService.updateProduct(updateProductRequest);

        EntityModel<ProductDto> resource = EntityModel.of(productDTO);
        resource.add(linkTo(methodOn(ProductController.class).createProduct(null)).withSelfRel());
        resource.add(linkTo(methodOn(ProductController.class).getProduct(productDTO.id())).withRel("get-product"));

        return ResponseEntity
                .created(linkTo(methodOn(ProductController.class).getProduct(productDTO.id())).withRel("get-product").toUri())
                .body(resource);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProductDto>> getProduct(@PathVariable("id") String id) {
        return productFacadeService.find(id)
                .map( productDto -> {

                    EntityModel<ProductDto> resource = EntityModel.of(productDto);
                    resource.add(linkTo(methodOn(ProductController.class).createProduct(null)).withSelfRel());
                    resource.add(linkTo(methodOn(ProductController.class).getProduct(productDto.id())).withRel("get-product"));

                    return ResponseEntity.ok().body(resource);
                })
                .orElseGet(()-> ResponseEntity.notFound().build());
    }
}
