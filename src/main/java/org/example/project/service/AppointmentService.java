package org.example.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.project.exception.AppointmentNotFoundException;
import org.example.project.exception.ConflictException;
import org.example.project.exception.UserNotFoundException;
import org.example.project.model.dto.request.AppointmentRequest;
import org.example.project.model.dto.response.AppointmentResponse;
import org.example.project.model.entity.Appointment;
import org.example.project.model.entity.User;
import org.example.project.model.enums.AppointmentStatus;
import org.example.project.model.enums.Role;
import org.example.project.repository.AppointmentRepository;
import org.example.project.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, AppointmentRequest request) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new UserNotFoundException(patientId));

        if (patient.getRole() != Role.PATIENT) {
            throw new ConflictException("Người dùng không phải bệnh nhân");
        }

        if (patientId.equals(request.getDoctorId())) {
            throw new ConflictException("Không thể đặt lịch khám với chính mình");
        }

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new UserNotFoundException(request.getDoctorId()));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ConflictException("Người dùng không phải bác sĩ");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ConflictException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        long minutes = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
        if (minutes < 15) {
            throw new ConflictException("Thời gian khám phải ít nhất 15 phút");
        }

        if (appointmentRepository.existsOverlappingAppointment(
                request.getDoctorId(), request.getDate(),
                request.getStartTime(), request.getEndTime())) {
            throw new ConflictException("Bác sĩ đã có lịch khám trong khoảng thời gian này");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(AppointmentStatus.PENDING)
                .symptomDescription(request.getSymptomDescription())
                .build();

        AppointmentResponse appointmentResponse = toResponse(appointmentRepository.save(appointment));
        appointmentResponse.setTime(appointment.getStartTime() + " - " + appointment.getEndTime());
//        appointmentResponse.setTime(appointment.getStartTime().toString().concat(" - ").concat(appointment.getEndTime().toString()));
        return appointmentResponse;
    }

    public Page<AppointmentResponse> getAllAppointmentsByPatient(Long patientId, int page, int size) {
        return appointmentRepository.findByPatientId(patientId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public Page<AppointmentResponse> getAllAppointmentsByDoctor(Long doctorId, int page, int size) {
        return appointmentRepository.findByDoctorId(doctorId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional
    public AppointmentResponse approveAppointment(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new AccessDeniedException("Không có quyền thực hiện thao tác này");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new ConflictException("Chỉ có thể duyệt lịch khám đang chờ xác nhận");
        }

        appointment.setStatus(AppointmentStatus.APPROVED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse rejectAppointment(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new AccessDeniedException("Không có quyền thực hiện thao tác này");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new ConflictException("Chỉ có thể từ chối lịch khám đang chờ xác nhận");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long requesterId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        boolean isPatient = appointment.getPatient().getId().equals(requesterId);
        boolean isDoctor = appointment.getDoctor().getId().equals(requesterId);

        if (!isPatient && !isDoctor) {
            throw new AccessDeniedException("Không có quyền thực hiện thao tác này");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING &&
                appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new ConflictException("Chỉ có thể hủy lịch khám đang chờ hoặc đã được duyệt");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientUsername(a.getPatient().getUsername())
                .doctorId(a.getDoctor().getId())
                .doctorUsername(a.getDoctor().getUsername())
                .date(a.getDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .symptomDescription(a.getSymptomDescription())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
