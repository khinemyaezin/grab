package com.product.domain.specification.framework;

public class OrSpecification<T> extends CompositeSpecification<T> implements Specification<T>{

    private final Specification<T> left;
    private final Specification<T> right;

    public OrSpecification(Specification<T> pLeft, Specification<T> pRight) {
        this.left = pLeft;
        this.right = pRight;
    }

    public boolean isSatisfiedBy(T t) {
        return left.isSatisfiedBy(t) || right.isSatisfiedBy(t);
    }
}