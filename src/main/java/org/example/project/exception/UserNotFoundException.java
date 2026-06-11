package org.example.project.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Không tìm thấy người dùng với id: " + id);
    }

    public UserNotFoundException(String username) {
        super("Không tìm thấy người dùng với tên đăng nhập: " + username);
    }
}