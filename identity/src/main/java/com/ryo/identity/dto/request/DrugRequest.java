package com.ryo.identity.dto.request;

import com.ryo.identity.entity.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class DrugRequest {
    private String name;
    private String content;
    private String document;
    private String slug;
    private Map<String, Object> metadata;
    private List<String> image;
    private List<String> ingredient;
    private Map<String, Object> info;

    private List<Integer> categoriesId;
}

