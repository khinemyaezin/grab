package com.product.infrastructure.mapper.category;

import com.nestedset.library.model.NodeComponent;
import com.product.domain.aggregate.category.CategoryComposite;
import com.product.domain.aggregate.category.CategoryLeaf;
import com.product.domain.aggregate.category.ICategory;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper
public abstract class NodeComponentMapper {
    public NodeComponent<ICategory> convert(NodeComponent<CategoryEntity> source) {
        if (source == null) {
            return null;
        }
        NodeComponent<ICategory> cat;
        if (source instanceof CategoryComposite) {
            cat = new CategoryComposite<>(source.getNode());
            for (NodeComponent<CategoryEntity> child : source.getChildren()) {
                var c = convert(child);
                c.setParent(cat);
                cat.addChild(c);
            }
        } else {
            cat = new CategoryLeaf<>(source.getNode());
        }
        return cat;
    }
}
