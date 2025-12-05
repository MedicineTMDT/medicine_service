package com.ryo.identity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySimpleResponse {
    private Integer id;

    private String name;
}
