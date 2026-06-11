package org.example.project.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.project.model.enums.Role;

@Data
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Pattern(regexp = "^\\S+$", message = "Tên đăng nhập không được chứa dấu cách")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có độ dài lớn hơn 6 ký tự")
    private String password;

    private Role role = Role.PATIENT;

    private Boolean isActive = true;
}