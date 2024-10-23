package com.product.domain.entity.category;

import com.nestedset.library.model.NodeComponent;
import java.util.Set;

public class CategoryLeaf extends AbstractCategory{
    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        super.uuid = uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        super.name = name;
    }

    @Override
    public Integer getLft() {
        return this.lft;
    }

    @Override
    public void setLft(Integer integer) {
        this.lft = integer;
    }

    @Override
    public Integer getRgt() {
        return this.rgt;
    }

    @Override
    public void setRgt(Integer integer) {
        this.rgt = integer;
    }

    @Override
    public Integer getDepth() {
        return this.depth;
    }

    @Override
    public void setDepth(Integer integer) {
        this.depth = integer;
    }

    @Override
    public Set<NodeComponent> getChildren() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void addChild(NodeComponent nodeComponent) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public NodeComponent getParent() {
        return this.parent;
    }

    @Override
    public void setParent(NodeComponent nodeComponent) {
        super.parent = (AbstractCategory) nodeComponent;
    }

    @Override
    public void print(String i) {
        System.out.println(i+name);
    }
}
