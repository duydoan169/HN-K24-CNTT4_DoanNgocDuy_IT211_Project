package org.example.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project.model.dto.request.AppointmentRequest;
import org.example.project.model.dto.response.ApiResponse;
import org.example.project.model.dto.response.AppointmentResponse;
import org.example.project.model.dto.response.MedicalRecordResponse;
import org.example.project.model.entity.User;
import org.example.project.service.AppointmentService;
import org.example.project.service.MedicalRecordService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patient/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt lịch khám thành công", appointmentService.bookAppointment(user.getId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAllAppointmentsByPatient(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử khám thành công", appointmentService.getAllAppointmentsByPatient(user.getId(), page, size)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Hủy lịch khám thành công", appointmentService.cancelAppointment(id, user.getId())));
    }

    @GetMapping("/{appointmentId}/records")
    public ResponseEntity<ApiResponse<Page<MedicalRecordResponse>>> getRecords(
            @AuthenticationPrincipal User user,
            @PathVariable Long appointmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ bệnh án thành công",
                medicalRecordService.getRecordsByAppointment(appointmentId, user.getId(), page, size)));
    }
}