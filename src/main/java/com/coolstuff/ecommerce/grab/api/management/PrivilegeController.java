package com.coolstuff.ecommerce.grab.api.management;

import com.coolstuff.ecommerce.grab.dto.global.ResponseHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/privileges")
public interface PrivilegeController {
    @PostMapping("")
    ResponseEntity<ResponseHolder> createPrivileges(@RequestBody String[] privileges);
}
