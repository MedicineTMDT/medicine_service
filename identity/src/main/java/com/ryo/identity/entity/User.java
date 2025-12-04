package com.ryo.identity.entity;

import com.ryo.identity.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "username", unique = true)
    String username;

    @Column(nullable = false)
    String avatarImg;

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    String firstName;

    @Column(nullable = false)
    String lastName;


    @Column(name = "email", unique = true, nullable = false)
    String email;

    Role role;

    String forgotPasswordToken;
    String verifyEmailToken;

    Boolean verifyEmail;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Request> requestList = new ArrayList<>();

}
