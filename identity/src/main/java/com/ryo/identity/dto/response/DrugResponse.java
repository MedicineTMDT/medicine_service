package com.ryo.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DrugResponse {
    private Integer id;
    private String name;
    private String slug;
    private Map<String, Object> metadata;
}

