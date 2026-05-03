package com.ryo.request.dto.request;

import com.ryo.request.constant.TypeOfRequest;
import lombok.Data;

@Data
public class CreateSuggestionRequest {
    private String title;
    private String content;
    private TypeOfRequest typeOfRequest;
}
