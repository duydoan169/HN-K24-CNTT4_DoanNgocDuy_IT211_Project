package org.example.project.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.project.model.enums.Role;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotNull(message = "Vai trò không được để trống")
    private Role role;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean isActive;
}
