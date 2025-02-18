package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.event.CategoryChangedEvent;
import com.product.domain.specification.UniqueProductVariantCompositeSpec;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Product extends AggregateRoot<Id> {
    private final List<ProductVariant> variants = new ArrayList<>();
    private final Set<VariantType> variantTypes = new HashSet<>();
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
        CompositeSpecification<Product> spec = new UniqueProductVariantCompositeSpec(variant);
        if (!spec.isSatisfiedBy(this)) return false;

        this.addVariantType(variant.getVariations());
        this.variants.add(variant);
        return true;
    }

    public boolean removeVariant(String uuid) {
        return variants.removeIf( v-> Objects.equals(uuid, v.getId()) );
    }

    private void addVariantType(Set<ProductVariation> productVariations) {
        for (ProductVariation v : productVariations) {
            this.variantTypes.add(v.getVariantOption().getVariantType());
        }
    }

    public Set<VariantType> getVariantTypes() {
        return Collections.unmodifiableSet(variantTypes);
    }

    /*private List<List<ProductVariation>> generateCombination(List<VariantType> types) {
        List<List<VariantOption>> optionLists = new ArrayList<>();
        for (VariantType variantType : types) {
            optionLists.add(variantType.getOptions());
        }

        List<List<ProductVariation>> combinations = new ArrayList<>();
        int totalCombinations = optionLists.stream()
                .mapToInt(List::size)
                .reduce(1, Math::multiplyExact);

        for (int i = 0; i < totalCombinations; i++) {
            List<VariantOption> combination = new ArrayList<>();
            int divisor = 1;
            for (List<VariantOption> options : optionLists) {
                int index = (i / divisor) % options.size();
                combination.add(options.get(index));
                divisor *= options.size();
            }
            combinations.add(combination.stream().map(c -> new ProductVariation(UUID.randomUUID().toString(), c)).toList());
        }
        return combinations;
    }*/

}
