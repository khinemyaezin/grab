package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;

import java.util.List;

public record ProductSpec(String name, Id categoryId, List<VariantType> desiredVariantTypes){ }

