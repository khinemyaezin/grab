package com.catalog.domain.aggregate;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ProductMedia extends Entity<Id> {
    private final String storageKey;
    private final String url;
    private final String contentType;
    private final int rank;

    public ProductMedia(Id id, String storageKey, String url, String contentType, int rank) {
        super(id);
        this.storageKey = Objects.requireNonNull(storageKey, "storageKey");
        this.url = url;
        this.contentType = contentType;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProductMedia that = (ProductMedia) o;
        return rank == that.rank
                && Objects.equals(storageKey, that.storageKey)
                && Objects.equals(url, that.url)
                && Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), storageKey, url, contentType, rank);
    }
}
