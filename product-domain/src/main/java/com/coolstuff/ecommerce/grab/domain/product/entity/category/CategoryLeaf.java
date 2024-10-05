package com.coolstuff.ecommerce.grab.domain.product.entity.category;

import com.coolstuff.core.nestedset.model.NodeComponent;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryLeaf extends AbstractCategory {
    private Long id;
    private String name;
    private Integer lft;
    private Integer rgt;
    private Integer depth;
    private NodeComponent parent;
    private boolean root;

    @Override
    public void print(String i) {
        System.out.println(i+name);
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}
