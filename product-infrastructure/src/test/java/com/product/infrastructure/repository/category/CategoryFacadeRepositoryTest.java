package com.product.infrastructure.repository.category;

import com.product.domain.entity.category.Category;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/test",
        "spring.datasource.username=admin",
        "spring.datasource.password=123",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.jpa.show-sql=true"
})
class CategoryFacadeRepositoryTest {
    @Autowired
    CategoryFacadeRepository categoryFacadeRepository;
    @Test
    void save() {
        Category category = new Category(UUID.randomUUID().toString(),"Root");
        categoryFacadeRepository.save(category);
        Category categoryClothing = new Category(UUID.randomUUID().toString(),"Clothing",category.getId());
        categoryFacadeRepository.save(categoryClothing);
        Category categoryShirt = new Category(UUID.randomUUID().toString(),"Shirt",categoryClothing.getId());
        categoryFacadeRepository.save(categoryShirt);
    }
}