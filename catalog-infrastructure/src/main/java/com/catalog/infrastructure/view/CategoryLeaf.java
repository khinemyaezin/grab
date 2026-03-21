package com.catalog.infrastructure.view;

import com.nestedset.library.model.NodeComponent;

import java.util.Set;

public class CategoryLeaf<T> extends NodeComponent<T> {
    private T node;
    private NodeComponent<T> parent;

    public CategoryLeaf(T node) {
        super(node);
        this.node = node;
    }

    @Override
    public T getNode() {
        return node;
    }

    @Override
    public void setNode(T node) {
        this.node = node;
    }

    @Override
    public Set<NodeComponent<T>> getChildren() {
        throw new UnsupportedOperationException("Not supported on leaf node.");
    }

    @Override
    public void addChild(NodeComponent<T> nodeComponent) {
        throw new UnsupportedOperationException("Not supported on leaf node.");
    }

    @Override
    public NodeComponent<T> getParent() {
        return parent;
    }

    @Override
    public void setParent(NodeComponent<T> parent) {
        this.parent = parent;
    }

    @Override
    public void print(String i) {
        // no-op
    }
}
