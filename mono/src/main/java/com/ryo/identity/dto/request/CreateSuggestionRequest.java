package com.ryo.identity.dto.request;

import com.ryo.identity.constant.TypeOfRequest;
import lombok.Data;

@Data
public class CreateSuggestionRequest {
    private String title;
    private String content;
    private TypeOfRequest typeOfRequest;
}
