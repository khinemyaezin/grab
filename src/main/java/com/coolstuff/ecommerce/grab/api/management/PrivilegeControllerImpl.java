package com.coolstuff.ecommerce.grab.api.management;

import com.coolstuff.ecommerce.grab.dto.global.ResponseHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrivilegeControllerImpl implements PrivilegeController {
    @Override
    public ResponseEntity<ResponseHolder> createPrivileges(String[] privileges) {
        return null;
    }
}
