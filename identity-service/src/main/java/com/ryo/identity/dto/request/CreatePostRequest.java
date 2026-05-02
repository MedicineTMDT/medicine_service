package com.ryo.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {
    @NotBlank(message = "NOT_BLANK")
    String title;

    @NotBlank(message = "NOT_BLANK")
    String content;

    Boolean proceed;

}
