package com.catalog.domain.aggregate;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.specification.CompositeSpecification;
import com.catalog.domain.event.CategoryChangedEvent;
import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.specification.UniqueProductVariantCompositeSpec;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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

    private Product(Id id, String name, Id categoryId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.categoryId = Objects.requireNonNull(categoryId);
    }

    public Product(Id id, String name, Id categoryId, List<ProductVariant> variants) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.variants.addAll(variants);
    }

    public static Product create(Id id, String name, Id categoryId) {
        return new Product(id, name, categoryId);
    }

    public void changeCategory(Id categoryId) {
        if (Objects.equals(this.categoryId, categoryId)) return;

        CategoryChangedEvent categoryChangedEvent = new CategoryChangedEvent(this.categoryId, categoryId, super.getId());
        this.categoryId = categoryId;

        super.addEvent(categoryChangedEvent);
    }

    public void delete() {
        ProductDeletedEvent productDeletedEvent = new ProductDeletedEvent(this.getId(),this.getCategoryId(),
                this.variants.stream().map(Entity::getId).toList());
        super.addEvent(productDeletedEvent);
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public List<ProductVariant> getActiveVariants() {
        return variants.stream()
                .filter(ProductVariant::isActive)
                .toList();
    }

    public List<ProductVariant> getDeletedVariants() {
        return variants.stream()
                .filter(ProductVariant::isDeleted)
                .toList();
    }

    public Optional<ProductVariant> findVariantById(Id id) {
        return variants.stream()
                .filter(v -> Objects.equals(v.getId(), id))
                .findFirst();
    }

    public boolean restoreVariant(Id id) {
        return findVariantById(id)
                .filter(ProductVariant::isDeleted)
                .map(v -> {
                    v.activate();
                    return true;
                })
                .orElse(false);
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
        super.addEvent(new ProductVariantChangeEvent(variant.getSku()));
        return true;
    }

    public boolean removeVariant(Id id) {
        boolean removed = variants.removeIf( v-> Objects.equals(id, v.getId()) );
        if(removed) {
            super.addEvent(new ProductVariantDeletedEvent(this.getId(),this.getCategoryId(), id));
        }
        return removed;
    }

    public void sortVariants(Comparator<ProductVariant> comparator) {
        this.variants.sort(comparator);
    }

    public void applySoftDeleteVariants(Collection<Id> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return;

        Set<Id> removalIds = new HashSet<>(variantIds.size());
        for (Id id : variantIds) {
            if (id != null) removalIds.add(id);
        }

        for (ProductVariant variant : this.variants) {
            if (variant.isActive() && removalIds.contains(variant.getId())) {
                variant.markAsDeleted();
            }
        }
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
