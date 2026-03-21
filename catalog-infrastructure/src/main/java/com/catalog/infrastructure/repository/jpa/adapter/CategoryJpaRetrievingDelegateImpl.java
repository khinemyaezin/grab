package com.catalog.infrastructure.repository.jpa.adapter;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRetrievingDelegate;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.delegate.jpa.JpaNestedSetRetrievingDelegate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CategoryJpaRetrievingDelegateImpl extends JpaNestedSetRetrievingDelegate<CategoryEntity, Long> implements CategoryJpaRetrievingDelegate {

    public CategoryJpaRetrievingDelegateImpl(
            JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config) {
        super(config);
    }

    @Override
    public List<CategoryEntity> getLeafNodesByName(String name) {
        if (name == null) {
            return Collections.emptyList();
        }

        String categoryName = name.trim();
        if (categoryName.isBlank()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        String escapedPrefix = escapeLike(categoryName.toLowerCase(Locale.ROOT)) + "%";

        CriteriaQuery<Object[]> matchQuery = cb.createQuery(Object[].class);
        Root<CategoryEntity> matchedNode = matchQuery.from(entityClassType);
        Path<String> matchedNodeName = matchedNode.get(configs.getNameFieldName());
        Expression<Integer> matchedNodeLeft = matchedNode.get(configs.getLeftFieldName()).as(Integer.class);
        Expression<Integer> matchedNodeRight = matchedNode.get(configs.getRightFieldName()).as(Integer.class);

        matchQuery.multiselect(matchedNodeLeft, matchedNodeRight)
                .where(cb.like(cb.lower(matchedNodeName), escapedPrefix, '\\'))
                .orderBy(cb.asc(matchedNodeLeft));

        List<Object[]> nodeMatches = entityManager.createQuery(matchQuery)
                .setMaxResults(1)
                .getResultList();
        if (nodeMatches.isEmpty()) {
            return Collections.emptyList();
        }

        Integer matchedLeft = (Integer) nodeMatches.getFirst()[0];
        Integer matchedRight = (Integer) nodeMatches.getFirst()[1];
        if (matchedLeft == null || matchedRight == null) {
            return Collections.emptyList();
        }

        CriteriaQuery<CategoryEntity> leafQuery = cb.createQuery(entityClassType);
        Root<CategoryEntity> leaf = leafQuery.from(entityClassType);

        Expression<Integer> leafLeft = leaf.get(configs.getLeftFieldName()).as(Integer.class);
        Expression<Integer> leafRight = leaf.get(configs.getRightFieldName()).as(Integer.class);

        leafQuery.select(leaf)
                .where(
                        cb.between(leafLeft, matchedLeft, matchedRight),
                        cb.equal(leafRight, cb.sum(leafLeft, 1))
                )
                .orderBy(cb.asc(leafLeft));

        return entityManager.createQuery(leafQuery).getResultList();
    }

    private String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

}
