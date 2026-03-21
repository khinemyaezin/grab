package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryLeavesResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
public class CategoryLeavesModelAssembler implements RepresentationModelAssembler<CategoryLeavesResponse, EntityModel<CategoryLeavesResponse>> {

    public EntityModel<CategoryLeavesResponse> toModel(CategoryLeavesResponse response, String name) {
        String encodedName = UriUtils.encodeQueryParam(name == null ? "" : name, StandardCharsets.UTF_8);
        return EntityModel.of(response,
                Link.of("/api/v1/categories/leaves?name=" + encodedName).withSelfRel()
        );
    }

    @Override
    public EntityModel<CategoryLeavesResponse> toModel(CategoryLeavesResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/categories/leaves").withSelfRel()
        );
    }
}
