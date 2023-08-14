package com.coolstuff.ecommerce.grab;

import com.coolstuff.ecommerce.grab.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = EcommerceApplication.class)
public class UserTest {
    @Autowired
    UserRepository userRepository;

    @Test
    public void getUser() {
        var user = this.userRepository.findByUserNameOrEmail("test@test.com").orElse(null);
        if(user!=null)
            System.out.println(user.getAuthorities());
    }
}
