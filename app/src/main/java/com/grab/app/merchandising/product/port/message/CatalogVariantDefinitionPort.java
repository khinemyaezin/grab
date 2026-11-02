package com.grab.app.merchandising.product.port.message;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Variant Types and Options definitions.
 * Replaces legacy VariantTypeController and VariantOptionController.
 */
public interface CatalogVariantDefinitionPort {

    List<VariantTypeResult> listVariantTypes();

    List<VariantOptionResult> listVariantOptions(Id variantTypeId);

    record VariantTypeResult(
            Id id,
            String name,
            String code,
            String description
    ) {}

    record VariantOptionResult(
            Id id,
            Id variantTypeId,
            String value,
            String label
    ) {}
}

