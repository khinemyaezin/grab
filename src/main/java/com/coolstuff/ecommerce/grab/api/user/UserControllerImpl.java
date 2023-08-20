package com.coolstuff.ecommerce.grab.api.user;

import com.coolstuff.ecommerce.grab.dto.UserData;
import com.coolstuff.ecommerce.grab.dto.global.ResponseHolder;
import com.coolstuff.ecommerce.grab.mapstruct.UserMapper;
import com.coolstuff.ecommerce.grab.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    @Override
    public ResponseEntity<ResponseHolder> getUsers() {
       return ResponseEntity.ok(new ResponseHolder("ok"));
    }

    @Override
    public ResponseEntity<UserData> getSessionUser(UserDetails userDetails) {
        return ResponseEntity.ok(this.userMapper.map( (UserEntity) userDetails) );
    }
}
