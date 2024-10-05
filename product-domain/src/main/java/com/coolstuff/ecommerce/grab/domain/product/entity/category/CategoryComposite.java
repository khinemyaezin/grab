package com.coolstuff.ecommerce.grab.domain.product.entity.category;

import com.coolstuff.core.nestedset.model.NodeComponent;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class CategoryComposite extends AbstractCategory {
    private Long id;
    private String name;
    private Integer lft;
    private Integer rgt;
    private Integer depth;
    private NodeComponent parent;
    private boolean root;
    private final Set<NodeComponent> children = new HashSet<>();

    @Override
    public void addSubNode(NodeComponent child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
       return this.name;
    }

    @Override
    public void print(String i) {
        System.out.println(i+name);

        for (NodeComponent child : children) {
            child.print(i+"-"); // Increase indentation for children
        }
    }
}
