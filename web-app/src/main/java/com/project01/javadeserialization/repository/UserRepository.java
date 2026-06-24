package com.project01.javadeserialization.repository;

import com.project01.javadeserialization.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}