package com.category.infrastructure.mapper;

import com.category.domain.aggregate.CategoryComposite;
import com.category.domain.aggregate.CategoryLeaf;
import com.category.domain.aggregate.ICategory;
import com.category.infrastructure.entity.CategoryEntity;
import com.nestedset.library.model.NodeComponent;
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
