package com.catalog.domain.aggregate;

import com.catalog.domain.event.CategoryChangedEvent;
import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductRestoredEvent;
import com.catalog.domain.event.ProductReviewRejectedEvent;
import com.catalog.domain.event.ProductReviewSubmittedEvent;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.event.ProductSuspendedEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.catalog.domain.exception.CatalogDomainError;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.valueobject.*;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A product has list of variants. Variant order can be neglect.
 * {Product -> [ {SKU1, GREEN, SMALL}, {SKU2, GREEN, LARGE} ]}
 */
public class Product extends AggregateRoot<Id> {
    private final List<ProductVariant> variants = new ArrayList<>();
    private final List<Description> descriptions = new ArrayList<>();
    private final List<ProductMedia> medias = new ArrayList<>();

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

    @Getter
    private Id sellerId;

    @Getter
    private SellerType sellerType;

    @Getter
    private ListingCondition listingCondition;

    @Getter
    private boolean offerEligible;

    @Getter
    private String moderationNote;

    private Product(Id id, String name, Id categoryId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.status = ProductStatus.DRAFT;
        this.slug = generateSlug(name);
        this.featured = false;
        this.offerEligible = false;
    }

    public Product(
            Id id,
            String name,
            Id categoryId,
            Id sellerId,
            SellerType sellerType,
            ListingCondition listingCondition,
            boolean offerEligible,
            ProductStatus status,
            String slug,
            boolean featured,
            String moderationNote,
            List<Description> descriptions,
            List<ProductMedia> medias,
            List<ProductVariant> variants
    ) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.sellerId = sellerId;
        this.sellerType = sellerType;
        this.listingCondition = listingCondition;
        this.offerEligible = offerEligible;
        this.status = status == null ? ProductStatus.DRAFT : status;
        this.slug = slug == null ? generateSlug(name) : slug;
        this.featured = featured;
        this.moderationNote = moderationNote;
        if (descriptions != null) {
            this.descriptions.addAll(descriptions);
        }
        if (medias != null) {
            this.medias.addAll(medias);
        }
        if (variants != null) {
            this.variants.addAll(variants);
        }
        validateOwnershipAndOfferPolicy();
    }

    public static Product create(Id id, String name, Id categoryId) {
        return new Product(id, name, categoryId);
    }

    public static Product create(
            Id id,
            String name,
            Id categoryId,
            Id sellerId,
            SellerType sellerType,
            ListingCondition condition,
            boolean offerEligible,
            boolean featured,
            String slug,
            List<Description> descriptions,
            List<ProductMedia> medias
    ) {
        Product product = new Product(id, name, categoryId);
        product.sellerId = sellerId;
        product.sellerType = sellerType;
        product.listingCondition = condition;
        product.offerEligible = offerEligible;
        product.featured = featured;
        if (slug != null && !slug.isBlank()) {
            product.slug = slug;
        }
        if (descriptions != null) {
            product.descriptions.addAll(descriptions);
        }
        if (medias != null) {
            product.medias.addAll(medias);
        }
        product.validateOwnershipAndOfferPolicy();
        return product;
    }

    public void changeCategory(Id categoryId) {
        if (Objects.equals(this.categoryId, categoryId)) {
            return;
        }

        CategoryChangedEvent categoryChangedEvent = new CategoryChangedEvent(this.categoryId, categoryId, super.getId());
        this.categoryId = categoryId;

        super.addEvent(categoryChangedEvent);
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public List<Description> getDescriptions() {
        return Collections.unmodifiableList(descriptions);
    }

    public List<ProductMedia> getMedias() {
        return Collections.unmodifiableList(medias);
    }

    public List<ProductVariant> getActiveVariants() {
        return variants.stream()
                .filter(ProductVariant::isActive)
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
        return addVariant(variant, this.variants.size());
    }

    public boolean addVariant(ProductVariant variant, int index) {
        if (hasDuplicateVariant(variant, index)) return false;
        this.variants.add(index, variant);
        return true;
    }

    private boolean hasDuplicateVariant(ProductVariant candidate, int ignoredVariantIndex) {
        for (int i = 0; i < variants.size(); i++) {
            if (ignoredVariantIndex >= 0 && i == ignoredVariantIndex) {
                continue;
            }
            ProductVariant existing = variants.get(i);
            boolean duplicateSku = existing.getSku().equalsIgnoreCase(candidate.getSku());
            boolean duplicateCombination = Objects.equals(existing.getVariations(), candidate.getVariations());
            if (duplicateSku || duplicateCombination) {
                return true;
            }
        }
        return false;
    }

    public boolean updateVariant(ProductVariant existing, ProductVariant newVariant) {
        int index = variants.indexOf(existing);
        if(index == -1)
            return false;

        if (hasDuplicateVariant(newVariant, index))
            return false;

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

    public ProductMetadata metadata() {
        return new ProductMetadata(
                name, categoryId, sellerId, sellerType,
                listingCondition, offerEligible, featured, slug, moderationNote
        );
    }

    public void updateMetadata(ProductMetadata next) {
        Objects.requireNonNull(next);

        if (metadata().equals(next)) {
            return;
        }

        if (!Objects.equals(this.categoryId, next.categoryId())) {
            changeCategory(next.categoryId());
        }

        this.name = next.name();
        this.sellerId = next.sellerId();
        this.sellerType = next.sellerType();
        this.listingCondition = next.condition();
        this.offerEligible = next.offerEligible();
        this.featured = next.featured();
        this.slug = next.slug();
        this.moderationNote = next.moderationNote();

        validateOwnershipAndOfferPolicy();
        addEvent(new ProductUpdatedEvent(getId(), this.name, this.categoryId));
    }

    public void replaceDescriptions(List<Description> descriptions) {
        List<Description> normalizedDescriptions = descriptions == null
                ? List.of()
                : new ArrayList<>(descriptions);

        if (Objects.equals(this.descriptions, normalizedDescriptions)) {
            return;
        }

        this.descriptions.clear();
        this.descriptions.addAll(normalizedDescriptions);
        super.addEvent(new ProductUpdatedEvent(this.getId(), this.name, this.categoryId));
    }

    public void replaceMedias(List<ProductMedia> medias) {
        List<ProductMedia> normalizedMedias = medias == null
                ? List.of()
                : new ArrayList<>(medias);

        if (Objects.equals(this.medias, normalizedMedias)) {
            return;
        }

        this.medias.clear();
        this.medias.addAll(normalizedMedias);
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

        if (newStatus == ProductStatus.IN_REVIEW) {
            ensureReadyForReview();
        }

        if (newStatus == ProductStatus.ACTIVE) {
            ensurePublishable();
        }

        ProductStatus old = this.status;
        this.status = newStatus;
        super.addEvent(new ProductStatusChangedEvent(this.getId(), old.name(), newStatus.name()));
    }

    public void submitForReview() {
        ensureReadyForReview();
        changeStatus(ProductStatus.IN_REVIEW);
        this.moderationNote = null;
        super.addEvent(new ProductReviewSubmittedEvent(this.getId(), this.getCategoryId()));
    }

    public void approve() {
        ensurePublishable();
        changeStatus(ProductStatus.ACTIVE);
    }

    public void reject(String reason) {
        this.moderationNote = reason;
        changeStatus(ProductStatus.DRAFT);
        super.addEvent(new ProductReviewRejectedEvent(this.getId(), reason));
    }

    public void suspend(String reason) {
        this.moderationNote = reason;
        changeStatus(ProductStatus.SUSPENDED);
        super.addEvent(new ProductSuspendedEvent(this.getId(), reason));
    }

    public void restore() {
        this.moderationNote = null;
        changeStatus(ProductStatus.DRAFT);
        super.addEvent(new ProductRestoredEvent(this.getId()));
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

    public void ensureReadyForReview() {
        validateOwnershipAndOfferPolicy();
        if (this.sellerId == null
                || this.sellerType == null
                || this.descriptions.isEmpty()
                || this.medias.isEmpty()) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.ListingIncomplete(),
                    "Listing is incomplete."
            );
        }
    }

    public void ensurePublishable() {
        ensureReadyForReview();
        if (this.getActiveVariants().isEmpty()) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.ProductActivationRequiresActiveVariants(),
                    "Cannot activate product without active variants."
            );
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

    private void validateOwnershipAndOfferPolicy() {
        if (this.offerEligible && this.sellerType != SellerType.C2C) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.OfferEligibilityOnlyForC2C(),
                    "Offer eligibility is only allowed for C2C listings."
            );
        }

        if (this.sellerType == SellerType.C2C && this.listingCondition == null) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.C2CConditionRequired(),
                    "C2C listings require item condition."
            );
        }
    }

    private static String generateSlug(String name) {
        if (name == null) {
            return null;
        }
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
