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
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 1 lowercase letter, 1 uppercase letter, 1 digit, 1 special character and must be at least 8 characters long"
    )
    String password;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "First name must contain only letters")
    @NotBlank
    String firstName;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name must contain only letters")
    @NotBlank
    String lastName;

    @Email
    @NotBlank
    String email;

    Role role;
}
