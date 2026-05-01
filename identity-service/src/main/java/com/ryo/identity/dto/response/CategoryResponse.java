package com.ryo.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponse {
    private Integer id;
    private String name;
    private Integer amount;
    private String slug;
    private LocalDateTime created;
}

