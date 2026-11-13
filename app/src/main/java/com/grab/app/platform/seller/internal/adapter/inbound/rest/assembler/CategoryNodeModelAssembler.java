package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryNodeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerCategoryApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CategoryNodeModelAssembler implements RepresentationModelAssembler<CategoryNodeResponse, EntityModel<CategoryNodeResponse>> {

    @Override
    public EntityModel<CategoryNodeResponse> toModel(CategoryNodeResponse node) {
        return toTreeModel(node, node == null ? null : node.id());
    }

    public EntityModel<CategoryNodeResponse> toTreeModel(CategoryNodeResponse node, String rootId) {
        if (node == null) {
            return null;
        }
        EntityModel<CategoryNodeResponse> model = EntityModel.of(node);
        model.add(SellerCategoryApiLinks.getCategoryTree(rootId).expand().withSelfRel());
        return model;
    }
}
