package com.product.domain.entity.category;

import com.nestedset.library.model.NodeComponent;

import java.util.HashSet;
import java.util.Set;

public class CategoryComposite<T> extends NodeComponent<T> {
    private T node;
    private NodeComponent<T> parent;
    private final Set<NodeComponent<T>> children = new HashSet<>();

    public CategoryComposite(T node) {
        super(node);
    }


    @Override
    public T getNode() {
        return node;
    }

    @Override
    public void setNode(T T) {
        node = T;
    }

    @Override
    public Set<NodeComponent<T>> getChildren() {
        return children;
    }

    @Override
    public void addChild(NodeComponent<T> nodeComponent) {
        children.add(nodeComponent);
    }

    @Override
    public NodeComponent<T> getParent() {
        return parent;
    }

    @Override
    public void setParent(NodeComponent<T> nodeComponent) {
        parent = nodeComponent;
    }

    @Override
    public void print(String i) {
        //System.out.println(i + node.get());

        for (NodeComponent<T> child : children) {
            child.print(i + "-"); // Increase indentation for children
        }
    }
}
