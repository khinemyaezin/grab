package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.event.CategoryChangedEvent;
import com.product.domain.specification.UniqueProductVariantCompositeSpec;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * A product has list of variants. Variant order can be neglect.
 * {Product -> [ {SKU1, GREEN, SMALL}, {SKU2, GREEN, LARGE} ]}
 */
public class Product extends AggregateRoot<Id> {
    private final List<ProductVariant> variants = new ArrayList<>();
    @Setter
    @Getter
    private String name;
    @Getter
    private Id categoryId;

    public Product(Id id, String name, Id categoryId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.categoryId = Objects.requireNonNull(categoryId);
    }

    public void changeCategory(Id categoryId) {
        if (Objects.equals(this.categoryId, categoryId)) return;

        CategoryChangedEvent categoryChangedEvent = new CategoryChangedEvent(this.categoryId, categoryId, super.id);
        this.categoryId = categoryId;

        super.addEvent(categoryChangedEvent);
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public boolean addVariant(ProductVariant variant) {
        return addVariant(variant, this.variants.size()); // Add at end
    }

    public boolean addVariant(ProductVariant variant, int index) {
        CompositeSpecification<Product> spec = new UniqueProductVariantCompositeSpec(variant);
        if (!spec.isSatisfiedBy(this)) return false;
        this.variants.add(index, variant);
        return true;
    }

    public boolean updateVariant(ProductVariant variant) {
        int index = variants.indexOf(variant);
        if(index == -1) return false;
        variants.set(index, variant);
        return true;
    }

    public boolean removeVariant(Id id) {
        return variants.removeIf( v-> Objects.equals(id, v.getId()) );
    }

    @Override
    public String toString() {
        String variants = this.variants.stream()
                .map(ProductVariant::toString)
                .reduce("", (a, b) -> a + "\n\t\t" + b);
        return "Product {" +
                "\n\tname='" + name + '\'' +
                "\n\tcategoryId=" + categoryId.getValue() +
                "\n\tvariants=[" + variants +
                "\n\t]" +
                "\n}";
    }
}
