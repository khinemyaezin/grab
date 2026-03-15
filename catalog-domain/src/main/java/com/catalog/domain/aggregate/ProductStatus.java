package com.catalog.domain.aggregate;

public enum ProductStatus {
    DRAFT, ACTIVE, ARCHIVED;

    public boolean canTransitionTo(ProductStatus target) {
        if (target == null) return false;
        return switch (this) {
            case DRAFT -> target == this || target == ACTIVE || target == ARCHIVED;
            case ACTIVE -> target == this || target == ARCHIVED;
            case ARCHIVED -> target == this || target == ACTIVE;
        };
    }
}
