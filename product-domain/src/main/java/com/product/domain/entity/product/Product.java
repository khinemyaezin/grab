package com.product.domain.entity.product;

import com.product.domain.entity.framework.AggregateRoot;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.entity.variant_type.VariantType;
import com.product.domain.event.CategoryChangedEvent;
import com.product.domain.specification.UniqueProductVariantCompositeSpec;
import com.product.domain.specification.framework.CompositeSpecification;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Product extends AggregateRoot<String> {
    private final List<ProductVariant> variants =  new ArrayList<>();
    private final Set<VariantType> variantTypes = new HashSet<>();
    @Setter
    @Getter
    private String name;
    @Getter
    private String categoryId;

    public Product(String id, String name, String categoryId ){
        super(id);
        this.name = name;
        this.categoryId = categoryId;
    }

    public void changeCategory(String categoryId) {
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

    public boolean updateVariant(ProductVariant variant) {
        int index = this.variants.indexOf(variant);
        if (index == -1) return false;

        ProductVariant oldVariant = this.variants.remove(index);

        CompositeSpecification<Product> spec = new UniqueProductVariantCompositeSpec(variant);
        if (!spec.isSatisfiedBy(this)) {
            this.variants.add(index, oldVariant);
            return false;
        }
        this.variants.add(index, variant);
        return true;
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
