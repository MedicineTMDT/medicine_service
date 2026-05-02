package com.ryo.identity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrugSimpleResponse {
    private Integer id;
    private String name;
    private String slug;
    private String imageLink;
}
