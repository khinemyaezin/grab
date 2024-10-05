package com.coolstuff.ecommerce.grab.infrastructure.product.mapper.category;

import com.coolstuff.core.nestedset.model.NodeComponent;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryComposite;
import com.coolstuff.ecommerce.grab.domain.product.generic.mapper.ObjectMapper;
import org.mapstruct.*;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CategoryCompositeMapper implements ObjectMapper<NodeComponent, CategoryComposite> {
    @Mapping(source = "children", target = "children")
    public abstract CategoryComposite convert(NodeComponent source);
}
