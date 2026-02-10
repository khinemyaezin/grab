package com.product.domain.valueobject;

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
    private final Id optionId;
    private final Id typeId;
    private final String typeName;

    public ProductVariation(String optionName, Id optionId, String typeName, Id typeId) {
        this.optionName = optionName;
        this.optionId = optionId;
        this.typeId = typeId;
        this.typeName = typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProductVariation that)) return false;
        return Objects.equals(optionId, that.optionId) && Objects.equals(typeId, that.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, typeId);
    }
}
