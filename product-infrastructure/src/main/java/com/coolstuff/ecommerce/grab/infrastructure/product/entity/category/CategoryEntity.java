package com.coolstuff.ecommerce.grab.infrastructure.product.entity.category;


import com.coolstuff.core.nestedset.columns.DepthColumn;
import com.coolstuff.core.nestedset.columns.LeftColumn;
import com.coolstuff.core.nestedset.columns.NameColumn;
import com.coolstuff.core.nestedset.columns.RightColumn;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.AbstractCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


@Getter
@Setter
@Entity
@Table(name = "category", indexes = {@Index(name = "leftIndex", columnList = "lft")})
public class CategoryEntity extends AbstractCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;

    @NameColumn
    private String name;

    @LeftColumn
    private Integer lft;

    @RightColumn
    private Integer rgt;

    @DepthColumn
    private Integer depth;

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public void setRoot(boolean root) {

    }
}
