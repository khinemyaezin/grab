package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.category.CategoryNodeInserter;
import com.catalog.adapter.persistence.category.CategoryNodeRemover;
import com.catalog.adapter.persistence.category.CategoryNodeRetriever;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.CategoryJpaRetrievingDelegate;
import com.catalog.adapter.persistence.repository.CategoryNestedSetNodeRepository;
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
