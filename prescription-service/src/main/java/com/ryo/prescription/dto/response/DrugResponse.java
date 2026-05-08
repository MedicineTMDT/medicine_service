package com.ryo.prescription.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugResponse {

    private Integer id;

    private String name;

    private String content;

    private String document;

    private String slug;

    private Map<String, Object> metadata;

    private List<String> image;

    private List<String> ingredient;

    private Map<String, Object> info;

    private Set<CategoryResponse> categories;

    private Set<MergedIngredientResponse> mergedIngredients;
}
