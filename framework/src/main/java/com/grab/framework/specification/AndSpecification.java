package com.grab.framework.specification;

public class AndSpecification <T> extends CompositeSpecification<T> {

    private final Specification<T> left;
    private final Specification<T> right;

    public AndSpecification(Specification<T> pLeft, Specification<T> pRight) {
        this.left = pLeft;
        this.right = pRight;
    }

    @Override
    public boolean isSatisfiedBy(T t) {
        return left.isSatisfiedBy(t) && right.isSatisfiedBy(t);
    }
}