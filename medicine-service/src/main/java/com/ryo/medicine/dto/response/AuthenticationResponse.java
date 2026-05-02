package com.ryo.medicine.dto.response;

import com.ryo.medicine.constant.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String id;
    String email;
    String username;
    String firstName;
    String lastName;
    Role role;
    String token;
    boolean authenticated;
}
