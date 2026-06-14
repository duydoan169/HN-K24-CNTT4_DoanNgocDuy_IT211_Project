package org.example.project.service;

import org.example.project.exception.ConflictException;
import org.example.project.exception.UserNotFoundException;
import org.example.project.model.dto.request.ChangePasswordRequest;
import org.example.project.model.dto.request.RegisterRequest;
import org.example.project.model.dto.request.UserUpdateRequest;
import org.example.project.model.dto.response.UserResponse;
import org.example.project.model.entity.User;
import org.example.project.model.enums.Role;
import org.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser()));

        userService.deleteUser(1L);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsConflict() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser()));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(ConflictException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePassword_SamePassword_ThrowsConflict() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("123456");
        request.setNewPassword("123456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser()));
        when(passwordEncoder.matches("123456", "hashedPassword")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void updateUser_Success() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updatedName");
        request.setRole(Role.DOCTOR);
        request.setIsActive(true);

        User updatedUser = mockUser();
        updatedUser.setUsername("updatedName");
        updatedUser.setRole(Role.DOCTOR);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser()));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(1L, request);

        assertEquals("updatedName", result.getUsername());
        assertEquals(Role.DOCTOR, result.getRole());
    }

    @Test
    void getAllUsers_ReturnsPaginatedResult() {
        Page<User> page = new PageImpl<>(List.of(mockUser()));
        when(userRepository.findByIsActive(true, PageRequest.of(0, 10))).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(0, 10);

        assertEquals(1, result.getTotalElements());
    }
}
