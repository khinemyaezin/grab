package com.grab.framework.cqrs;

public record PageInfo(
        int page,
        int size,
        long totalElements,
        int totalPages
) {

}
