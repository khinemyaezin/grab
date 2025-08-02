package com.product.domain.aggregate.product;

import com.grab.framework.domain.ValueObject;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.Objects;

/**
 * A variation has one option.
 * {Color}
 */
@Getter
public class ProductVariation extends ValueObject {
    private final String optionName; // yellow
    private Id optionId; // nullable
    private String typeName;

    public ProductVariation(String optionName, String typeName) {
        this.optionName = optionName;
        this.typeName = typeName;
    }

    public ProductVariation(String optionName, Id optionId) {
        this.optionName = optionName;
        this.optionId = optionId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProductVariation that)) return false;
        if(Objects.isNull(optionId)) {
            return (that.optionName != null ? that.optionName.equalsIgnoreCase(optionName) : optionName == null)
                    && (that.typeName != null ? that.typeName.equalsIgnoreCase(typeName) : typeName == null);
        } else {
            return Objects.equals(optionId, that.optionId);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, typeName, optionId);
    }
}
