package com.product.domain.entity.category;

import com.nestedset.library.model.NodeComponent;

public abstract class AbstractCategory extends NodeComponent implements ICategory{
    protected String uuid;
    protected String name;
    protected Integer lft;
    protected Integer rgt;
    protected Integer depth;
    protected AbstractCategory parent;
}
