package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerCategoryApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryModelAssembler implements RepresentationModelAssembler<CategoryResponse, EntityModel<CategoryResponse>> {

    @Override
    public EntityModel<CategoryResponse> toModel(CategoryResponse category) {
        return assemble(category, true);
    }

    public EntityModel<CategoryResponse> toCommandModel(CategoryResponse category) {
        return assemble(category, false);
    }

    private EntityModel<CategoryResponse> assemble(CategoryResponse category, boolean canonicalRead) {
        if (category == null) {
            return null;
        }
        EntityModel<CategoryResponse> model = EntityModel.of(category);
        if (category.id() != null) {
            Link retrieval = SellerCategoryApiLinks.getCategory(category.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            model.add(SellerCategoryApiLinks.getChildren(category.id()));
            model.add(SellerCategoryApiLinks.deleteCategory(category.id()));
        }
        model.add(SellerCategoryApiLinks.getCategoryTree(null));
        return model;
    }

    public CollectionModel<EntityModel<CategoryResponse>> getChildrenModel(List<CategoryResponse> responses, String categoryId) {
        List<EntityModel<CategoryResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerCategoryApiLinks.getChildren(categoryId).expand();
        CollectionModel<EntityModel<CategoryResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        model.add(SellerCategoryApiLinks.saveCategory());
        return model;
    }

    public CollectionModel<EntityModel<CategoryResponse>> getLeafNodesModel(List<CategoryResponse> responses, String name) {
        List<EntityModel<CategoryResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerCategoryApiLinks.getLeafNodes(name).expand();
        CollectionModel<EntityModel<CategoryResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        model.add(SellerCategoryApiLinks.saveCategory());
        return model;
    }
}
