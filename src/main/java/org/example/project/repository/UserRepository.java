package org.example.project.repository;

import org.example.project.model.entity.User;
import org.example.project.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseAndIsActive(String username, Boolean isActive, Pageable pageable);

    Page<User> findByRoleAndIsActive(Role role, Boolean isActive, Pageable pageable);
}
