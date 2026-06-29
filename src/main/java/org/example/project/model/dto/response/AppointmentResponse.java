package org.example.project.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.project.model.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientUsername;
    private Long doctorId;
    private String doctorUsername;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
    private String symptomDescription;
    private LocalDateTime createdAt;
    private String time;
}
