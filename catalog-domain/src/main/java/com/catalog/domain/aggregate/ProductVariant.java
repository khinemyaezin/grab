package com.catalog.domain.aggregate;

import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.*;

/**
 * A variant has a set of variations.
 * {Variant 1 , [ Color, Size ]}
 */
@Getter
public class ProductVariant extends Entity<Id> {
    private final String sku;
    private final Set<ProductVariation> variations ;
    private final boolean manageInventory;
    private ProductVariantStatus status;
    private final LinkedHashSet<Id> mediaIds = new LinkedHashSet<>();
    private Id thumbnailMediaId;

    public ProductVariant(
            Id id,
            String sku,
            ProductVariantStatus status,
            List<ProductVariation> variations,
            boolean manageInventory,
            Collection<Id> mediaIds,
            Id thumbnailMediaId
    ) {
        super(id);
        this.sku = sku;
        this.status = status;
        this.variations = new LinkedHashSet<>(variations == null ? List.of() : variations);
        this.manageInventory = manageInventory;
        if (mediaIds != null) {
            this.mediaIds.addAll(mediaIds);
        }
        this.thumbnailMediaId = thumbnailMediaId;
    }

    public static ProductVariant create(Id id, String sku, List<ProductVariation> variations) {
        return create(id, sku, variations, false);
    }

    public static ProductVariant create(Id id, String sku, List<ProductVariation> variations, boolean manageInventory) {
        return new ProductVariant(id, sku, ProductVariantStatus.ACTIVE, variations, manageInventory, List.of(), null);
    }

    public Set<Id> getMediaIds() {
        return Collections.unmodifiableSet(mediaIds);
    }

    void replaceMedia(Collection<Id> nextMediaIds, Id nextThumbnailMediaId) {
        this.mediaIds.clear();
        if (nextMediaIds != null) {
            this.mediaIds.addAll(nextMediaIds);
        }
        this.thumbnailMediaId = nextThumbnailMediaId;
    }

    void retainOwnedMedia(Set<Id> ownedMediaIds) {
        this.mediaIds.removeIf(id -> !ownedMediaIds.contains(id));
        if (this.thumbnailMediaId != null && !ownedMediaIds.contains(this.thumbnailMediaId)) {
            this.thumbnailMediaId = null;
        }
    }

    public void markAsDeleted() {
        this.status = ProductVariantStatus.DELETED;
    }

    public void activate() {
        this.status = ProductVariantStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == ProductVariantStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == ProductVariantStatus.DELETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        String optionName = variations.stream()
                .map( v-> v.getOptionId().getValue())
                .reduce("",(a,b)-> a+b);
        return this.getId() + " : " + this.sku + " : " + optionName + " [" + status + "]";
    }
}
