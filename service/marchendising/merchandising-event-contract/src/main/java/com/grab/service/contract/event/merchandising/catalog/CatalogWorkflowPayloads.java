package com.grab.service.contract.event.merchandising.catalog;

import com.grab.framework.id.Id;

import java.util.List;

public final class CatalogWorkflowPayloads {

    private CatalogWorkflowPayloads() {
    }

    public record VariantUpdate(Id merchantId, Id productId, Id variantId, String sku, Boolean manageInventory) {
    }

    public record VariantUpdated(String sku) {
    }

    public record ProductCreate(
            Id merchantId,
            String name,
            Id categoryId,
            String condition,
            String slug,
            List<VariantType> variantTypes,
            List<Variant> variants
    ) {
        public ProductCreate {
            variantTypes = List.copyOf(variantTypes);
            variants = List.copyOf(variants);
        }
    }

    public record ProductUpdate(
            Id merchantId,
            Id productId,
            String variantSyncIntent,
            List<VariantType> variantTypes,
            String name,
            Id categoryId,
            String condition,
            String slug,
            List<Variant> variants
    ) {
        public ProductUpdate {
            variantTypes = List.copyOf(variantTypes);
            variants = List.copyOf(variants);
        }
    }

    public record ProductCreated(Id productId, List<Variant> variants) {
        public ProductCreated {
            variants = List.copyOf(variants);
        }
    }

    public record ProductUpdated(List<Variant> variants) {
        public ProductUpdated {
            variants = List.copyOf(variants);
        }
    }

    public record Variant(
            Id variantId,
            String sku,
            String matrixKey,
            Boolean manageInventory,
            List<Variation> variations
    ) {
        public Variant {
            variations = List.copyOf(variations);
        }
    }

    public record Variation(Id typeId, Id optionId) {
    }

    public record VariantType(Id typeId, List<VariantOption> options) {
        public VariantType {
            options = List.copyOf(options);
        }
    }

    public record VariantOption(Id optionId, String optionName) {
    }

    public record Media(Id id, String storageKey, String contentType, Integer rank) {
    }

    public record Description(Id id, String name, String title, String description) {
    }

    public record MediaReplacement(Id merchantId, Id productId, List<Media> media) {
        public MediaReplacement {
            media = List.copyOf(media);
        }
    }

    public record DescriptionReplacement(Id merchantId, Id productId, List<Description> descriptions) {
        public DescriptionReplacement {
            descriptions = List.copyOf(descriptions);
        }
    }

    public record StatusChange(Id merchantId, Id productId, String status) {
    }

    public record Eligibility(Id merchantId, Id productId, boolean publishing) {
    }

    public record Publication(Id salesChannelId, Id variantId, String sku) {
    }

    public record Publications(Id merchantId, Id productId, List<Publication> targets, List<Variant> variants) {
        public Publications {
            targets = List.copyOf(targets);
            variants = List.copyOf(variants);
        }
    }

    public record PublicationChanges(List<Publication> targets) {
        public PublicationChanges {
            targets = List.copyOf(targets);
        }
    }
}
