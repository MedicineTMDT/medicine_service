package com.ryo.medicine.dto.request;

import com.ryo.medicine.constant.TypeOfRequest;
import lombok.Data;

@Data
public class CreateSuggestionRequest {
    private String title;
    private String content;
    private TypeOfRequest typeOfRequest;
}
