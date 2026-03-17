package com.catalog.domain.valueobject;

public enum ProductStatus {
    DRAFT,
    IN_REVIEW,
    ACTIVE,
    ARCHIVED,
    SUSPENDED;

    public boolean canTransitionTo(ProductStatus target) {
        if (target == null) return false;
        return switch (this) {
            case DRAFT -> target == this || target == IN_REVIEW || target == ACTIVE || target == ARCHIVED || target == SUSPENDED;
            case IN_REVIEW -> target == this || target == ACTIVE || target == DRAFT || target == ARCHIVED || target == SUSPENDED;
            case ACTIVE -> target == this || target == ARCHIVED || target == SUSPENDED;
            case ARCHIVED -> target == this || target == DRAFT || target == ACTIVE || target == SUSPENDED;
            case SUSPENDED -> target == this || target == DRAFT || target == ACTIVE || target == ARCHIVED;
        };
    }
}
