package com.ryo.medicine.dto.request;

import com.ryo.medicine.constant.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserRequest {
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 5, max = 20, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 6, max = 20, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "NOT_BLANK")
    String firstName;

    @NotBlank(message = "NOT_BLANK")
    String lastName;

    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "NOT_BLANK")
    String email;

    Role role;
}
