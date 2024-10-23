package com.product.domain.entity.category;

import com.nestedset.library.model.NodeComponent;

import java.util.HashSet;
import java.util.Set;

public class CategoryComposite extends AbstractCategory {
    private final Set<AbstractCategory> children = new HashSet<>();

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
    public String toString() {
        return super.name;
    }

    @Override
    public Integer getLft() {
        return super.lft;
    }

    @Override
    public void setLft(Integer integer) {
        super.lft = integer;
    }

    @Override
    public Integer getRgt() {
        return super.rgt;
    }

    @Override
    public void setRgt(Integer integer) {
        super.rgt = integer;
    }

    @Override
    public Integer getDepth() {
        return super.depth;
    }

    @Override
    public void setDepth(Integer integer) {
        super.depth = integer;
    }

    @Override
    public Set<NodeComponent> getChildren() {
        return new HashSet<>(children);
    }

    @Override
    public void addChild(NodeComponent nodeComponent) {
        this.children.add((AbstractCategory) nodeComponent);
    }

    @Override
    public NodeComponent getParent() {
        return super.parent;
    }

    @Override
    public void setParent(NodeComponent nodeComponent) {
        super.parent = (AbstractCategory) nodeComponent;
    }

    @Override
    public void print(String i) {
        System.out.println(i + name);

        for (NodeComponent child : children) {
            child.print(i + "-"); // Increase indentation for children
        }
    }
}
