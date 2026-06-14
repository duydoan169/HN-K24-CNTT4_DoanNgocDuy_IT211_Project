package org.example.project.service;

import org.example.project.exception.ConflictException;
import org.example.project.exception.UserNotFoundException;
import org.example.project.model.dto.request.RegisterRequest;
import org.example.project.model.dto.response.UserResponse;
import org.example.project.model.entity.User;
import org.example.project.model.enums.Role;
import org.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser() {
        return User.builder()
                .id(1L)
                .username("patient1")
                .passwordHash("hashedPassword")
                .role(Role.PATIENT)
                .isActive(true)
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("patient1");
        request.setPassword("123456");
        request.setRole(Role.PATIENT);
        request.setIsActive(true);

        when(userRepository.existsByUsername("patient1")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser());

        UserResponse result = userService.register(request);

        assertNotNull(result);
        assertEquals("patient1", result.getUsername());
        assertEquals(Role.PATIENT, result.getRole());
    }

    @Test
    void register_DuplicateUsername_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("patient1");
        request.setPassword("123456");

        when(userRepository.existsByUsername("patient1")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser()));

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("patient1", result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void deleteUser_AlreadyInactive_ThrowsConflict() {
        User inactiveUser = mockUser();
        inactiveUser.setIsActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(inactiveUser));

        assertThrows(ConflictException.class, () -> userService.deleteUser(1L));
    }
}