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

    @NotBlank
    String userId;

    @NotBlank
    @Size(min = 5, max = 20, message = "username must be between {min} to {max} characters")
    String username;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "First name must contain only letters")
    @NotBlank
    String firstName;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name must contain only letters")
    @NotBlank
    String lastName;

    @Email
    @NotBlank
    String email;

    String avatarImg;
}
