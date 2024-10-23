package com.product.infrastructure.integration.category;

import com.nestedset.library.model.NestedSet;
import com.nestedset.library.model.NodeComponent;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractTreeBuilder implements TreeBuilder {
    private final NodeComponentFactory nodeComponentFactory;

    protected AbstractTreeBuilder(NodeComponentFactory nodeComponentFactory) {
        this.nodeComponentFactory = nodeComponentFactory;
    }

    @Override
    public Optional<NodeComponent> buildTree(List<NestedSet> nodeList) {
        NestedSet root = nodeList.getFirst();
        NodeComponent node;
        if (root.getRgt() == root.getLft() + 1) {
            node = nodeComponentFactory.createLeafNodeComponent();
            this.merge(root,node);
            return Optional.of(node);
        } else {
            node = nodeComponentFactory.createCompositeNodeComponent();
            this.merge(root,node);
            return Optional.of(buildTreeRecursive(node, nodeList,0));
        }
    }
    @Override
    public Optional<NodeComponent> buildTree(Collection<? extends NestedSet> input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }

        List<NestedSet> nodeList = input.stream()
                .sorted(Comparator.comparingInt(NestedSet::getLft)) // Sorting by ascending lft
                .collect(Collectors.toList());

        return this.buildTree(nodeList);
    }

    private NodeComponent buildTreeRecursive(NodeComponent parent, List<NestedSet> nodeList, int index) {
        int i = index + 1;
        while (i < nodeList.size() && nodeList.get(i).getLft() < parent.getRgt()) {
            NestedSet child = nodeList.get(i);
            if (child.getDepth() == parent.getDepth() + 1) {
                NodeComponent node;
                if (child.getRgt() == child.getLft() + 1) {
                    node = nodeComponentFactory.createLeafNodeComponent();
                } else {
                    node = nodeComponentFactory.createCompositeNodeComponent();
                }
                this.merge(child,node);
                node.setParent(parent);
                parent.addChild(buildTreeRecursive(node, nodeList,i));
            }
            i++;
        }
        return parent;
    }
    @Override
    public List<NodeComponent> getLeafList(NodeComponent node) {
        List<NodeComponent> leafNodes = new ArrayList<>();

        // Base case: If the node is null, return an empty list
        if (node == null) {
            return leafNodes;
        }

        if (node.getRgt() == node.getLft()+1) {
            leafNodes.add(node);
        } else {
            for (NodeComponent child : node.getChildren()) {
                leafNodes.addAll(getLeafList(child));
            }
        }
        return leafNodes;
    }

    public abstract void merge(NestedSet source, NodeComponent target);
}
