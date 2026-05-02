package com.ryo.identity.dto.request;

import lombok.Data;

@Data
public class MergedIngredientRequest {
    private String name;
    private String sourceTable;
    private Integer sourceId;
}
