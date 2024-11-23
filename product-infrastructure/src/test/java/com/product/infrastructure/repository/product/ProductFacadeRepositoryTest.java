package com.product.infrastructure.repository.product;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.entity.variant_option.VariantOption;
import com.product.domain.entity.variant_type.VariantType;
import com.product.domain.repository.product.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.stream.IntStream;

@Disabled
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/test",
        "spring.datasource.username=admin",
        "spring.datasource.password=123",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.jpa.show-sql=true"
})
@Sql({"/mock-category.sql","/mock-variant-type-and-option.sql"})
class ProductFacadeRepositoryTest {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductEntityRepository productEntityRepository;

    Product product;

    @BeforeEach
    void crateProductDomain() {
        var color = new VariantType("b7922f9f-1fe9-4e51-9949-982a7398c92a", "Color");

        var yellow = new VariantOption("94f86eab-8be2-4cee-9303-3340728fcda7", "Yellow", color);
        var blue = new VariantOption("8578eda9-fc45-44fe-af5d-0ded9a9bc54d", "Blue", color);
        color.addOption(yellow);
        color.addOption(blue);

        var size =  new VariantType("71269f58-4d53-4f08-8678-74487ad99217", "Size");

        var small = new VariantOption("128cb594-740c-4735-89f3-c930d2af0b7e", "Small",size);
        size.addOption(small);

        product = new Product("c9ac1163-e67e-4ea4-ad40-2eaf093d6b76", "Shirt", "f765-5678-2345-7890");

        ProductVariant variant = new ProductVariant("2c095488-b386-4861-8234-cb8896a401a1",product.getId(), "SKU-1",
                List.of(new ProductVariation(yellow),new ProductVariation(small))
        );
        ProductVariant variant2 = new ProductVariant("5e600051-2b27-4fa7-a967-9701a6b62bd7",product.getId(), "SKU-2",
                List.of(new ProductVariation(blue),new ProductVariation( small))
        );

        product.addVariant(variant);
        product.addVariant(variant2);
    }

    @Test
    void save() {
        productRepository.save(product);
    }

    @Test
    void find() {
        productRepository.save(product);
        var optionalProduct = productRepository.find(product.getId());
        Assertions.assertTrue(optionalProduct.isPresent());
        Assertions.assertEquals( product.getId(), optionalProduct.get().getId());
        Assertions.assertEquals( product.getVariants().size(), optionalProduct.get().getVariants().size());
        Assertions.assertTrue( allMatch(product.getVariants(), optionalProduct.get().getVariants()));
    }

    public static boolean allMatch(List<ProductVariant> list1, List<ProductVariant> list2) {
        if (list1.size() != list2.size()) {
            return false; // If sizes are different, they cannot fully match
        }
        return IntStream.range(0, list1.size())
                .allMatch(i -> list1.get(i).equals(list2.get(i)));
    }
}