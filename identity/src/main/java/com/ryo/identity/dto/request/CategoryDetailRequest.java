package com.ryo.identity.dto.request;

import lombok.Data;

@Data
public class CategoryDetailRequest {
    private String name;
    private String content;
    private Integer categoryId;
}