package com.grab.app.merchandising.catalog.port.message;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Variant Types and Options definitions.
 * Replaces legacy VariantTypeController and VariantOptionController.
 */
public interface CatalogVariantDefinitionPort {

    List<VariantTypeMessageResult> listVariantTypes(String name);

    List<VariantOptionMessageResult> listVariantOptions(String name, Id variantTypeId);

    record VariantTypeMessageResult(
            String id,
            String name
    ) {}

    record VariantOptionMessageResult(
            String id,
            String name,
            String typeId,
            String typeName
    ) {}
}
