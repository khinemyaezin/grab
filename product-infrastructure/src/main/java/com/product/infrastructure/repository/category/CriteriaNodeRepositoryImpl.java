package com.product.infrastructure.repository.category;

import com.nestedset.library.annotation.DepthColumn;
import com.nestedset.library.annotation.LeftColumn;
import com.nestedset.library.annotation.NameColumn;
import com.nestedset.library.annotation.RightColumn;
import com.product.infrastructure.entity.category.CategoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Repository
public class CriteriaNodeRepositoryImpl implements CriteriaNodeRepository<CategoryEntity, Long>{
    @PersistenceContext
    protected EntityManager entityManager;
    
    protected static NodeField configs;
    static {
        NodeField config = new NodeField();

        Map<Class<? extends Annotation>, Consumer<String>> annotationToSetter = new HashMap<>();
        annotationToSetter.put(Id.class, config::setIdFieldName);
        annotationToSetter.put(NameColumn.class, config::setNameFieldName);
        annotationToSetter.put(LeftColumn.class, config::setLeftFieldName);
        annotationToSetter.put(RightColumn.class, config::setRightFieldName);
        annotationToSetter.put(DepthColumn.class, config::setDepthFieldName);

        for (Field field : CategoryEntity.class.getDeclaredFields()) {
            for (Map.Entry<Class<? extends Annotation>, Consumer<String>> entry : annotationToSetter.entrySet()) {
                if (field.isAnnotationPresent(entry.getKey())) {
                    entry.getValue().accept(field.getName());
                    break;
                }
            }
        }

        configs =  config;
    }

    protected List<CategoryEntity> executeQuery(CriteriaQuery<CategoryEntity> query) {
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Integer getMaxRight() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> cq = cb.createQuery(CategoryEntity.class);
        Root<CategoryEntity> queryRoot = cq.from(CategoryEntity.class);

        cq.orderBy(cb.desc(queryRoot.get(configs.getRightFieldName())));
        List<CategoryEntity> highestRows = executeQuery(cq);

        if (highestRows.isEmpty()) {
            return 0;
        } else {
            return highestRows.getFirst().getRgt();
        }
    }

    @Override
    public void incrementLeftBoundaryAfter(Integer right) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> cq = criteriaBuilder.createQuery(CategoryEntity.class);
        Root<CategoryEntity> root = cq.from(CategoryEntity.class);
        Predicate predicate = criteriaBuilder.greaterThan(root.get(configs.getLeftFieldName()), right);
        cq.where(predicate);

        List<CategoryEntity> result = entityManager.createQuery(cq).getResultList();

        for (CategoryEntity node : result) {
            node.setLft(node.getRgt() + 2);
            entityManager.merge(node);
        }
    }

    @Override
    public void incrementRightBoundaryAfter(Integer right) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> cq = criteriaBuilder.createQuery(CategoryEntity.class);
        Root<CategoryEntity> root = cq.from(CategoryEntity.class);
        Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(root.get(configs.getRightFieldName()), right);
        cq.where(predicate);

        List<CategoryEntity> result = entityManager.createQuery(cq).getResultList();

        for (CategoryEntity node : result) {
            node.setRgt(node.getRgt() + 2);
            entityManager.merge(node);
        }
    }

    @Override
    public void decrementLeftBoundaryAfter(Integer right, Integer width) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> cq = criteriaBuilder.createQuery(CategoryEntity.class);
        Root<CategoryEntity> root = cq.from(CategoryEntity.class);
        Predicate predicate = criteriaBuilder.greaterThan(root.get(configs.getLeftFieldName()), right);
        cq.where(predicate);

        List<CategoryEntity> result = entityManager.createQuery(cq).getResultList();

        for (CategoryEntity node : result) {
            node.setLft(node.getLft() - width);
            entityManager.merge(node);
        }
    }

    @Override
    public void decrementRightBoundaryAfter(Integer right, Integer width) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> cq = criteriaBuilder.createQuery(CategoryEntity.class);
        Root<CategoryEntity> root = cq.from(CategoryEntity.class);
        Predicate predicate = criteriaBuilder.greaterThan(root.get(configs.getRightFieldName()), right);
        cq.where(predicate);

        List<CategoryEntity> result = entityManager.createQuery(cq).getResultList();

        for (CategoryEntity node : result) {
            node.setRgt(node.getRgt() - width);
            entityManager.merge(node);
        }
    }

    @Override
    public void removeNodesInRange(Integer left, Integer right) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<CategoryEntity> query = criteriaBuilder.createCriteriaDelete(CategoryEntity.class);
        Root<CategoryEntity> root = query.from(CategoryEntity.class);

        query.where(
                criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get(configs.getLeftFieldName()), left),
                        criteriaBuilder.lessThanOrEqualTo(root.get(configs.getRightFieldName()), right)
                )
        );
        entityManager.createQuery(query).executeUpdate();
    }

    //@Override
    public List<CategoryEntity> getChildren(Integer left, Integer right) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> cq = cb.createQuery(CategoryEntity.class);
        Root<CategoryEntity> root = cq.from(CategoryEntity.class);

        Predicate leftPredicate = cb.greaterThanOrEqualTo(root.get(configs.getLeftFieldName()), left);
        Predicate rightPredicate = cb.lessThanOrEqualTo(root.get(configs.getRightFieldName()), right);
        cq.where(cb.and(leftPredicate, rightPredicate))
                .orderBy(cb.asc(root.get(configs.getLeftFieldName())));

        return executeQuery(cq);
    }

    /**
     * FIND THE IMMEDIATE SUBORDINATES OF A NODE
     * Imagine you are showing a CategoryEntity of electronics products on a retailer web site.
     * When a user clicks on a CategoryEntity, you would want to show the products of that CategoryEntity,
     * as well as list its immediate sub-categories, but not the entire tree of categories beneath it.
     * For this, we need to show the node and its immediate sub-nodes, but no further down the tree. For example,
     * when showing the PORTABLE ELECTRONICS CategoryEntity,
     * we will want to show MP3 PLAYERS, CD PLAYERS, and 2 WAY RADIOS, but not FLASH.
     * This can be easily accomplished by adding a HAVING clause to our previous query:
     */
    @Override
    public List<CategoryEntity> getImmediateChildren(Long nodeId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> query = cb.createQuery(CategoryEntity.class);

        Root<CategoryEntity> node = query.from(CategoryEntity.class);
        Root<CategoryEntity> parent = query.from(CategoryEntity.class);
        Root<CategoryEntity> subParent = query.from(CategoryEntity.class);

        Subquery<Integer> subQuery = query.subquery(Integer.class);
        Root<CategoryEntity> subNode = subQuery.from(CategoryEntity.class);
        Root<CategoryEntity> subParentNode = subQuery.from(CategoryEntity.class);

        subQuery.select(cb.diff(cb.count(subParentNode), 1).as(Integer.class))
                .where(
                        cb.between(subNode.get(configs.getLeftFieldName()), subParentNode.get(configs.getLeftFieldName()), subParentNode.get(configs.getRightFieldName())),
                        cb.equal(subNode.get(configs.getIdFieldName()), nodeId)
                )
                .groupBy(subNode.get(configs.getIdFieldName()), subNode.get(configs.getNameFieldName()), subNode.get(configs.getLeftFieldName()));


        query.multiselect(
                        node.get(configs.getIdFieldName()),
                        node.get(configs.getNameFieldName()),
                        node.get(configs.getLeftFieldName()),
                        node.get(configs.getRightFieldName()),
                        cb.diff(cb.count(parent), cb.sum(subQuery.getSelection(), 1)).as(Integer.class)
                )
                .where(
                        cb.between(node.get(configs.getLeftFieldName()), parent.get(configs.getLeftFieldName()), parent.get(configs.getRightFieldName())),
                        cb.between(node.get(configs.getLeftFieldName()), subParent.get(configs.getLeftFieldName()), subParent.get(configs.getRightFieldName())),
                        cb.equal(subParent.get(configs.getIdFieldName()), nodeId)
                )
                .groupBy(node.get(configs.getIdFieldName()), node.get(configs.getNameFieldName()), subQuery.getSelection(), node.get(configs.getLeftFieldName()))
                .having(cb.le(cb.diff(cb.count(parent), cb.sum(subQuery.getSelection(), 1)), 1))
                .orderBy(cb.asc(node.get(configs.getLeftFieldName())));

        return executeQuery(query);

    }

    @Override
    public List<CategoryEntity> getParentOf(Long Long) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEntity> query = criteriaBuilder.createQuery(CategoryEntity.class);
        Root<CategoryEntity> parent = query.from(CategoryEntity.class);
        Root<CategoryEntity> node = query.from(CategoryEntity.class);

        Predicate leftBetween = criteriaBuilder.between(node.get(configs.getLeftFieldName()), parent.get(configs.getLeftFieldName()), parent.get(configs.getRightFieldName()));
        Predicate nodeIdMatch = criteriaBuilder.and(criteriaBuilder.equal(node.get(configs.getIdFieldName()), Long));

        query.select(parent)
                .where(leftBetween, nodeIdMatch)
                .orderBy(criteriaBuilder.asc(parent.get(configs.getLeftFieldName())));

        return this.executeQuery(query);
    }
}
