package com.ryo.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryDetailResponse {
    private Integer id;
    private String name;
    private String content;
    private LocalDateTime created;
    private LocalDateTime update;
}