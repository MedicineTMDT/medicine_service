package com.ryo.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditUserRequest {
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 5, max = 20, message = "USERNAME_INVALID")
    String username;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "FIRSTNAME_INVALID")
    @NotBlank(message = "NOT_BLANK")
    String firstName;

    @NotBlank(message = "NOT_BLANK")
    String lastName;

    String avatarImg;
}
