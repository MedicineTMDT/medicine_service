package com.ryo.prescription.dto.request;

import com.ryo.prescription.constant.TypeOfRequest;
import lombok.Data;

@Data
public class CreateSuggestionRequest {
    private String title;
    private String content;
    private TypeOfRequest typeOfRequest;
}
