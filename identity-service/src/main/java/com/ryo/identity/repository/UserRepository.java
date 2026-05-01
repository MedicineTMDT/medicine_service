package com.ryo.identity.repository;

import com.ryo.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String adminUserName);
    boolean existsByEmailAndIdNot(String email, String id);
    boolean existsByUsernameAndIdNot(String username, String id);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
}
