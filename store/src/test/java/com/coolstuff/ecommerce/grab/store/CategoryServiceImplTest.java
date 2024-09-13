package com.coolstuff.ecommerce.grab.store;

import com.coolstuff.ecommerce.grab.infrastructure.product.repository.category.CategoryEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class CategoryServiceImplTest {
    @Autowired
    CategoryEntityRepository repository;

    @Test
    public void test(){
       var c = this.repository.findByUuid("12345");
        System.out.println(c.get().getId());
    }


}