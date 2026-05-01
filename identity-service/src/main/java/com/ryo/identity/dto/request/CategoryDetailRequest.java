package com.ryo.identity.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDetailRequest {
    private String name;
    private String content;
    private Integer categoryId;
}