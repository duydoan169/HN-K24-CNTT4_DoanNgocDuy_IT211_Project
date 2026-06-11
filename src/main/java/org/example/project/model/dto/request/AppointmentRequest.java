package org.example.project.model.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest {

    @NotNull(message = "Mã bác sĩ không được để trống")
    private Long doctorId;

    @NotNull(message = "Ngày khám không được để trống")
    @Future(message = "Ngày khám phải là ngày trong tương lai")
    private LocalDate date;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    private String symptomDescription;
}
