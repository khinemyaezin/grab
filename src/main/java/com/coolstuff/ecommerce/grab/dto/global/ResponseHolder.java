package com.coolstuff.ecommerce.grab.dto.global;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResponseHolder<T> {
    private T data;
}
