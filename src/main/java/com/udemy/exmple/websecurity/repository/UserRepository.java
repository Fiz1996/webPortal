package com.udemy.exmple.websecurity.repository;

import com.udemy.exmple.websecurity.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users,Long> {
    Users findUsersByUsername(String username);

    Users findUsersByEmail(String email);
}
