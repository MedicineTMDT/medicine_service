package com.ryo.identity.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {
    @NotBlank
    String title;

    @NotBlank
    String content;

    Boolean proceed;

}
