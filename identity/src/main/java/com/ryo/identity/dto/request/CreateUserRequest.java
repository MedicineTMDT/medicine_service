package com.ryo.identity.dto.request;

import com.ryo.identity.constant.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.antlr.v4.runtime.misc.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserRequest {
    @NotBlank
    @Size(min = 5, max = 20, message = "username must be between {min} to {max} characters")
    String username;

    @NotBlank
    @Size(min = 6, max = 20, message = "Password must be between {min} to {max} characters")
    String password;

    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    @Email
    @NotBlank
    String email;

    Role role;
}
