package com.coolstuff.ecommerce.grab.domain.product.entity.category;

import com.coolstuff.core.nestedset.model.NodeComponent;

public abstract class AbstractCategory extends NodeComponent{
    public abstract boolean isRoot();
    public abstract void setRoot(boolean root);
}
