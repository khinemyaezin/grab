package com.coolstuff.ecommerce.grab.api.user;

import com.coolstuff.ecommerce.grab.dto.UserData;
import com.coolstuff.ecommerce.grab.dto.global.ResponseHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/users")
public interface UserController {
    @GetMapping("")
    ResponseEntity<ResponseHolder> getUsers();

    @GetMapping("/me")
    ResponseEntity<UserData> getSessionUser(@AuthenticationPrincipal UserDetails userDetails);
}
