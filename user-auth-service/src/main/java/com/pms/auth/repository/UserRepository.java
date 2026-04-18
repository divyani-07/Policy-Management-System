package com.pms.auth.repository;

import com.pms.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository gives you free methods: save(), findById(), findAll(), delete() etc.
// <User, Long> = Entity type, Primary Key type
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring auto-generates SQL from method name:
    // SELECT * FROM tbl_users WHERE username = ?
    Optional<User> findByUsername(String username);
    // Optional = might return a User, or might return empty (not null)

    // Check if username already exists — to prevent duplicates
    boolean existsByUsername(String username);

    // Check if email already exists — to prevent duplicates
    boolean existsByEmail(String email);

    // Pageable = Spring's built-in pagination object
    // Page<User> = result wrapper with data + page metadata
    Page<User> findAll(Pageable pageable);
    // Spring generates: SELECT * FROM tbl_users LIMIT ? OFFSET ?
}