package com.catalog.domain.valueobject;

import com.grab.framework.id.Id;

import java.util.Objects;

public record ProductMediaKey(String value) {

    public ProductMediaKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("storageKey is required");
        }
        if (containsPathTraversal(value)) {
            throw new IllegalArgumentException("storageKey must not contain path traversal");
        }
    }

    public boolean isStaged() {
        String[] parts = value.split("/");
        return parts.length >= 4 && "merchants".equals(parts[0]) && "staged".equals(parts[2]);
    }

    public boolean isOwnedBy(Id merchantId) {
        return merchantId != null
                && merchantId.getValue() != null
                && value.startsWith("merchants/" + merchantId.getValue() + "/");
    }

    public boolean isForeignStaged(Id merchantId) {
        return isStaged() && !isOwnedBy(merchantId);
    }

    public ProductMediaKey promoteToProduct(Id productId) {
        if (!isStaged()) {
            throw new IllegalArgumentException("storageKey is not staged");
        }
        Objects.requireNonNull(productId, "productId");
        String merchantId = value.split("/")[1];
        return new ProductMediaKey("merchants/%s/products/%s/%s".formatted(
                merchantId,
                productId.getValue(),
                fileName()
        ));
    }

    public String fileName() {
        int slash = value.lastIndexOf('/');
        return slash >= 0 ? value.substring(slash + 1) : value;
    }

    private static boolean containsPathTraversal(String key) {
        for (String part : key.split("/")) {
            if ("..".equals(part)) {
                return true;
            }
        }
        return false;
    }

    public static final class Factory {
        private final Id merchantId;

        public Factory(Id merchantId) {
            this.merchantId = Objects.requireNonNull(merchantId, "merchantId");
        }

        public ProductMediaKey staged(Id objectId, String extension) {
            Objects.requireNonNull(objectId, "objectId");
            return new ProductMediaKey("merchants/%s/staged/%s%s".formatted(
                    merchantId.getValue(),
                    objectId.getValue(),
                    extension == null ? "" : extension
            ));
        }

        public ProductMediaKey forProduct(Id productId, Id objectId, String extension) {
            Objects.requireNonNull(productId, "productId");
            Objects.requireNonNull(objectId, "objectId");
            return new ProductMediaKey("merchants/%s/products/%s/%s%s".formatted(
                    merchantId.getValue(),
                    productId.getValue(),
                    objectId.getValue(),
                    extension == null ? "" : extension
            ));
        }
    }
}
