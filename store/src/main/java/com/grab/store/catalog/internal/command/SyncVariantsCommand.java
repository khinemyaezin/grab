package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

import java.util.List;

public record SyncVariantsCommand(
        Id merchantId,
        Id productId,
        List<VariantType> variantTypes,
        List<Variant> variants
) implements Command<SyncVariantsResult> {

    public record VariantType(
            Id typeId,
            String typeName,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            Id optionId,
            String optionName
    ) {}

    public record Variant(
            Id id,
            String sku,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionName,
            Id optionId,
            Id typeId,
            String typeName
    ) {}
}
