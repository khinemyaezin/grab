package com.catalog.infrastructure.entity.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
public class ProductPublicationEntityId implements Serializable {
    private Long variantId;
    private String salesChannelId;

    public ProductPublicationEntityId() {
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductPublicationEntityId that)) {
            return false;
        }
        return Objects.equals(variantId, that.variantId)
                && Objects.equals(salesChannelId, that.salesChannelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantId, salesChannelId);
    }
}
