package com.coolstuff.ecommerce.grab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserData {
    private String name;
    private String email;
    private String avatar;
    private String[] permissions;
}
