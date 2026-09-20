package com.catalog.adapter.persistence.mapper;

import com.nestedset.library.model.NodeComponent;
import com.catalog.adapter.persistence.category.CategoryComposite;
import com.catalog.adapter.persistence.category.CategoryLeaf;
import com.catalog.adapter.persistence.category.ICategory;
import com.catalog.adapter.persistence.entity.CategoryEntity;
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
