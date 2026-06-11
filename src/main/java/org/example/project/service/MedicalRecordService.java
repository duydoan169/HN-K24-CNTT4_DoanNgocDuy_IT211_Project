package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.exception.AppointmentNotFoundException;
import org.example.project.exception.ConflictException;
import org.example.project.model.dto.response.MedicalRecordResponse;
import org.example.project.model.entity.Appointment;
import org.example.project.model.entity.MedicalRecord;
import org.example.project.model.enums.AppointmentStatus;
import org.example.project.repository.AppointmentRepository;
import org.example.project.repository.MedicalRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final CloudinaryService cloudinaryService;

    public MedicalRecordResponse uploadRecord(Long doctorId, Long appointmentId, MultipartFile file, String diagnosis) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new AccessDeniedException("Không có quyền thực hiện thao tác này");
        }

        if (appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new ConflictException("Chỉ có thể tải hồ sơ cho lịch khám đã được duyệt");
        }

        String fileUrl = cloudinaryService.upload(file);

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .fileUrl(fileUrl)
                .diagnosis(diagnosis)
                .build();

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return toResponse(medicalRecordRepository.save(record));
    }

    public Page<MedicalRecordResponse> getRecordsByAppointment(Long appointmentId, Long requesterId, int page, int size) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        boolean isPatient = appointment.getPatient().getId().equals(requesterId);
        boolean isDoctor = appointment.getDoctor().getId().equals(requesterId);

        if (!isPatient && !isDoctor) {
            throw new AccessDeniedException("Không có quyền thực hiện thao tác này");
        }

        return medicalRecordRepository.findByAppointmentId(appointmentId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .id(r.getId())
                .appointmentId(r.getAppointment().getId())
                .fileUrl(r.getFileUrl())
                .diagnosis(r.getDiagnosis())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
