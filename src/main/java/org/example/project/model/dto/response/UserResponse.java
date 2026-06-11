package org.example.project.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.project.model.enums.Role;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
