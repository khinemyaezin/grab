package com.coolstuff.ecommerce.grab.utility;

import com.coolstuff.ecommerce.grab.persistence.entity.UserEntity;
import com.coolstuff.ecommerce.grab.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class UserUtility {
    private final UserRepository userRepository;
    public String generateUsername(UserEntity user) {
        String baseUsername = generateBaseUserName(user);

        if (!isUserNameExisted(baseUsername)) {
            return baseUsername;
        }

        int suffix = 1;
        String newUsername;
        do {
            newUsername = baseUsername + suffix++;
        } while (isUserNameExisted(newUsername));

        return newUsername;
    }

    private String generateBaseUserName(UserEntity user) {
        String[] parts = user.getEmail().split("@");
        if (parts.length == 2) {
            String emailName = parts[0];

            String username = emailName.replaceAll("[^a-zA-Z0-9]", "");
            return  username.toLowerCase();
        } else {
            throw new IllegalArgumentException("Invalid email address format");
        }
    }

    public boolean isUserNameExisted(String username){
        if(!StringUtils.hasText(username)) return false;
        return this.userRepository.findByUserNameOrEmail(username).isPresent();
    }
}
