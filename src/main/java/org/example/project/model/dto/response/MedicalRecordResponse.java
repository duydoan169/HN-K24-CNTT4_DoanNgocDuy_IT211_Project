package org.example.project.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MedicalRecordResponse {
    private Long id;
    private Long appointmentId;
    private String fileUrl;
    private String diagnosis;
    private LocalDateTime createdAt;
}
