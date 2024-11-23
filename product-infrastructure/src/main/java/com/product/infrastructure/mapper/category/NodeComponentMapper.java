package com.product.infrastructure.mapper.category;

import com.nestedset.library.model.NodeComponent;
import com.product.domain.entity.category.CategoryComposite;
import com.product.domain.entity.category.CategoryLeaf;
import com.product.domain.entity.category.ICategory;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.mapper.ObjectMapper;
import org.mapstruct.Mapper;

@Mapper
public abstract class NodeComponentMapper implements ObjectMapper<NodeComponent<CategoryEntity>, NodeComponent<ICategory>> {
    @Override
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
