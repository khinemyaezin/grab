package com.grab.store.product.mapper.category;

import com.grab.store.product.mapper.ObjectMapper;
import com.grab.store_interface.product.dto.category.ReadableCategory;
import com.grab.store_interface.product.dto.product.PersistableProduct;
import com.nestedset.library.model.NodeComponent;
import com.product.domain.entity.category.AbstractCategory;
import com.product.domain.entity.product.Product;
import org.mapstruct.Mapper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mapper
public abstract class ReadableCategoryMapper implements ObjectMapper<AbstractCategory, ReadableCategory> {

    @Override
    public ReadableCategory convert(AbstractCategory source) {
        return convert(source, source.getUuid());
    }

    private ReadableCategory convert(AbstractCategory source, String parentUuid) {
        if (source == null) {
            return null;
        }

        Set<ReadableCategory> readableChildren = new HashSet<>();

        for (NodeComponent child : source.getChildren()) {
            readableChildren.add(convert((AbstractCategory) child, source.getUuid()));
        }

        // Return the new ReadableCategory object
        return new ReadableCategory(
                source.getUuid(),
                source.getName(),
                source.getDepth(),
                parentUuid, // Set the parent here
                readableChildren
        );
    }
}
