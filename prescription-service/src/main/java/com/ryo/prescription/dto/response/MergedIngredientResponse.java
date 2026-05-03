package com.ryo.prescription.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MergedIngredientResponse {
    private Integer id;
    private String name;
//    private String sourceTable;
//    private Integer sourceId;
}
