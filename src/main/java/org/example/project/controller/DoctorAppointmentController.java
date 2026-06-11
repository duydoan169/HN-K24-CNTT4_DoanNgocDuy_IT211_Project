package org.example.project.controller;

import lombok.RequiredArgsConstructor;
import org.example.project.model.dto.response.ApiResponse;
import org.example.project.model.dto.response.AppointmentResponse;
import org.example.project.model.entity.User;
import org.example.project.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/doctor/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAllAppointmentsByDoctor(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch khám thành công", appointmentService.getAllAppointmentsByDoctor(user.getId(), page, size)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AppointmentResponse>> approveAppointment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Duyệt lịch khám thành công", appointmentService.approveAppointment(id, user.getId())));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rejectAppointment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Từ chối lịch khám thành công", appointmentService.rejectAppointment(id, user.getId())));
    }
}
