package com.pms.auth.repository;

import com.pms.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository gives you free methods: save(), findById(), findAll(), delete() etc.
// <User, Long> = Entity type, Primary Key type
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring auto-generates SQL from method name:
    // SELECT * FROM tbl_users WHERE username = ?
    Optional<User> findByUsername(String username);
    // Optional = might return a User, or might return empty (not null)
}