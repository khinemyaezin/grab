package com.catalog.domain.valueobject;

public enum ProductStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED,
    SUSPENDED;

    public boolean canTransitionTo(ProductStatus target) {
        if (target == null) return false;
        return switch (this) {
            case DRAFT     -> target == this || target == ACTIVE || target == ARCHIVED || target == SUSPENDED;
            case ACTIVE    -> target == this || target == ARCHIVED || target == SUSPENDED;
            case ARCHIVED  -> target == this || target == DRAFT || target == SUSPENDED;
            case SUSPENDED -> target == this || target == DRAFT || target == ARCHIVED;
        };
    }
}
