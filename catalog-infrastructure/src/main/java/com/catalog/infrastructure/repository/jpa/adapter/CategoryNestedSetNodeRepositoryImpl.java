package com.catalog.infrastructure.repository.jpa.adapter;

import com.catalog.infrastructure.adapter.category.CategoryNodeInserter;
import com.catalog.infrastructure.adapter.category.CategoryNodeRemover;
import com.catalog.infrastructure.adapter.category.CategoryNodeRetriever;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRetrievingDelegate;
import com.catalog.infrastructure.repository.jpa.CategoryNestedSetNodeRepository;
import com.nestedset.app.DelegatingNestedSetNodeRepository;
import com.nestedset.app.service.TreeBuilder;

import java.util.List;

public class CategoryNestedSetNodeRepositoryImpl extends DelegatingNestedSetNodeRepository<CategoryEntity, Long> implements CategoryNestedSetNodeRepository {

    private final CategoryJpaRetrievingDelegate retrievingDelegate;

    public CategoryNestedSetNodeRepositoryImpl(CategoryNodeInserter inserter,
                                               CategoryNodeRemover remover,
                                               CategoryNodeRetriever retriever,
                                               TreeBuilder<CategoryEntity, Long> treeBuilder,
                                               CategoryJpaRetrievingDelegate retrievingDelegate) {
        super(inserter, remover, retriever, treeBuilder);
        this.retrievingDelegate = retrievingDelegate;
    }

    @Override
    public List<CategoryEntity> findLeafNodeBy(String name) {
        return retrievingDelegate.getLeafNodesByName(name);
    }
}
