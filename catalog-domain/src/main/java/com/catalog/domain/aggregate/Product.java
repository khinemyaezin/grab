package com.catalog.domain.aggregate;

import com.catalog.domain.exception.CatalogDomainError;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.specification.CompositeSpecification;
import com.catalog.domain.event.CategoryChangedEvent;
import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
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

    @Getter
    private ProductStatus status;

    @Getter
    private String slug;

    @Getter
    private boolean featured;

    private Product(Id id, String name, Id categoryId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.status = ProductStatus.DRAFT;
        this.slug = generateSlug(name);
        this.featured = false;
    }

    public Product(
            Id id,
            String name,
            Id categoryId,
            ProductStatus status,
            String slug,
            boolean featured,
            List<ProductVariant> variants) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.categoryId = Objects.requireNonNull(categoryId);
        if (variants != null) this.variants.addAll(variants);
        this.status = status == null ? ProductStatus.DRAFT : status;
        this.slug = slug == null ? generateSlug(name) : slug;
        this.featured = featured;
    }

    public static Product create(Id id, String name, Id categoryId) {
        return new Product(id, name, categoryId);
    }

    public static Product create(Id id, String name, Id categoryId, boolean featured, String slug) {
        Product product = new Product(id, name, categoryId);
        product.featured = featured;
        if (slug != null && !slug.isBlank()) {
            product.slug = slug;
        }
        return product;
    }

    public void changeCategory(Id categoryId) {
        if (Objects.equals(this.categoryId, categoryId)) return;

        CategoryChangedEvent categoryChangedEvent = new CategoryChangedEvent(this.categoryId, categoryId, super.getId());
        this.categoryId = categoryId;

        super.addEvent(categoryChangedEvent);
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
                    super.addEvent(new ProductVariantRestoredEvent(this.getId(), v.getId()));
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

    public boolean updateVariant(ProductVariant existing, ProductVariant newVariant) {
        int index = variants.indexOf(existing);
        if(index == -1) return false;

        CompositeSpecification<Product> spec = new UniqueProductVariantCompositeSpec(newVariant, index);
        if (!spec.isSatisfiedBy(this)) return false;

        variants.set(index, newVariant);
        super.addEvent(new ProductVariantChangeEvent(newVariant.getSku()));
        return true;
    }

    public void removeVariant(Id id) {
        ensureLastActiveVariantIsNotRemoved(Set.of(id));

        Optional<ProductVariant> existing = findVariantById(id);
        boolean removed = variants.removeIf(v -> Objects.equals(id, v.getId()));
        if (removed) {
            super.addEvent(new ProductVariantDeletedEvent(this.getId(), this.getCategoryId(), id));
            if (existing.filter(ProductVariant::isActive).isPresent()) {
                archiveIfUnsellable();
            }
        }
    }

    public void applySoftDeleteVariants(Collection<Id> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return;

        Set<Id> removalIds = new HashSet<>(variantIds.size());
        for (Id id : variantIds) {
            if (id != null) removalIds.add(id);
        }

        ensureLastActiveVariantIsNotRemoved(removalIds);

        for (ProductVariant variant : this.variants) {
            if (variant.isActive() && removalIds.contains(variant.getId())) {
                variant.markAsDeleted();
                super.addEvent(new ProductVariantDeletedEvent(this.getId(), this.getCategoryId(), variant.getId()));
            }
        }

        archiveIfUnsellable();
    }

    public void update(String name, Id categoryId, boolean featured, String slug) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(categoryId);

        boolean nameChanged = !Objects.equals(this.name, name);
        boolean categoryChanged = !Objects.equals(this.categoryId, categoryId);
        boolean featuredChanged = this.featured != featured;
        boolean slugChanged = slug != null && !slug.isBlank() && !Objects.equals(this.slug, slug);

        if (!nameChanged && !categoryChanged && !featuredChanged && !slugChanged) return;

        if (nameChanged) {
            this.name = name;
            this.slug = slug != null && !slug.isBlank() ? slug : generateSlug(name);
        } else if (slugChanged) {
            this.slug = slug;
        }

        if (categoryChanged) {
            changeCategory(categoryId);
        }

        this.featured = featured;

        super.addEvent(new ProductUpdatedEvent(this.getId(), this.name, this.categoryId));
    }

    public boolean isVisibleOnStorefront() {
        return this.status == ProductStatus.ACTIVE && !this.getActiveVariants().isEmpty();
    }

    public void changeStatus(ProductStatus newStatus) {
        Objects.requireNonNull(newStatus);
        if (Objects.equals(this.status, newStatus)) return;

        if (!this.status.canTransitionTo(newStatus)) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.InvalidProductStatusTransition(this.status.name(), newStatus.name()),
                    "Invalid status transition from " + this.status + " to " + newStatus + "."
            );
        }

        if (newStatus == ProductStatus.ACTIVE && this.getActiveVariants().isEmpty()) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.ProductActivationRequiresActiveVariants(),
                    "Cannot activate product without active variants."
            );
        }

        ProductStatus old = this.status;
        this.status = newStatus;
        super.addEvent(new ProductStatusChangedEvent(this.getId(), old.name(), newStatus.name()));
    }

    public void delete() {
        if (this.status == ProductStatus.ARCHIVED) {
            super.addEvent(new ProductDeletedEvent(
                    this.getId(),
                    this.getCategoryId(),
                    this.variants.stream().map(Entity::getId).toList()
            ));
            return;
        }
        changeStatus(ProductStatus.ARCHIVED);
        super.addEvent(new ProductDeletedEvent(
                this.getId(),
                this.getCategoryId(),
                this.variants.stream().map(Entity::getId).toList()
        ));
    }

    public void archiveIfUnsellable() {
        if (this.status == ProductStatus.ACTIVE && this.getActiveVariants().isEmpty()) {
            changeStatus(ProductStatus.ARCHIVED);
        }
    }

    private void ensureLastActiveVariantIsNotRemoved(Set<Id> candidateVariantIds) {
        if (this.status != ProductStatus.ACTIVE || candidateVariantIds == null || candidateVariantIds.isEmpty()) {
            return;
        }

        long activeVariantsToDelete = this.variants.stream()
                .filter(ProductVariant::isActive)
                .filter(variant -> candidateVariantIds.contains(variant.getId()))
                .count();

        if (activeVariantsToDelete <= 0 || this.getActiveVariants().size() - activeVariantsToDelete > 0) {
            return;
        }

        Id protectedVariantId = this.variants.stream()
                .filter(ProductVariant::isActive)
                .filter(variant -> candidateVariantIds.contains(variant.getId()))
                .map(Entity::getId)
                .findFirst()
                .orElse(null);

        throw new CatalogDomainValidationException(
                new CatalogDomainError.CannotDeleteLastActiveVariantFromActiveProduct(
                        protectedVariantId == null ? null : protectedVariantId.getValue()
                ),
                "Cannot delete the last active variant from an active product."
        );
    }

    private static String generateSlug(String name) {
        if (name == null) return null;
        String s = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("(^-+)|(-+$)", "");
        return s;
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
