package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.exception.ConflictException;
import org.example.project.exception.UserNotFoundException;
import org.example.project.model.dto.request.ChangePasswordRequest;
import org.example.project.model.dto.request.RegisterRequest;
import org.example.project.model.dto.request.UserRequest;
import org.example.project.model.dto.request.UserUpdateRequest;
import org.example.project.model.dto.response.UserResponse;
import org.example.project.model.entity.User;
import org.example.project.model.enums.Role;
import org.example.project.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Tên đăng nhập đã tồn tại");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(request.getIsActive())
                .build();

        return toResponse(userRepository.save(user));
    }

    public Page<UserResponse> getAllUsers(int page, int size) {
        return userRepository.findByIsActive(true, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public Page<UserResponse> searchUsers(String username, int page, int size) {
        return userRepository.findByUsernameContainingIgnoreCaseAndIsActive(username, true, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public Page<UserResponse> getUsersByRole(Role role, int page, int size) {
        return userRepository.findByRoleAndIsActive(role, true, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public UserResponse getUserById(Long id) {
        return toResponse(findById(id));
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findById(id);
        user.setUsername(request.getUsername());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive());
        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        if (!user.getIsActive()) {
            throw new ConflictException("Tài khoản đã bị vô hiệu hóa trước đó");
        }
        user.setIsActive(false);
        userRepository.save(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ConflictException("Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ConflictException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
