package com.ryo.identity.entity;

import com.ryo.identity.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "username", unique = true,
            columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    @Column(nullable = false)
    String avatarImg;

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    String firstName;

    @Column(nullable = false)
    String lastName;


    @Column(name = "username", unique = true, nullable = false,
            columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String email;

    Role role;
}
