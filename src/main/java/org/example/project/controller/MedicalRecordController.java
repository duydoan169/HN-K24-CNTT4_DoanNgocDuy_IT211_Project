package org.example.project.controller;

import lombok.RequiredArgsConstructor;
import org.example.project.model.dto.response.ApiResponse;
import org.example.project.model.dto.response.MedicalRecordResponse;
import org.example.project.model.entity.User;
import org.example.project.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/doctor/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping(value = "/{appointmentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> uploadRecord(
            @AuthenticationPrincipal User user,
            @PathVariable Long appointmentId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String diagnosis) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải hồ sơ bệnh án thành công",
                        medicalRecordService.uploadRecord(user.getId(), appointmentId, file, diagnosis)));
    }
}
