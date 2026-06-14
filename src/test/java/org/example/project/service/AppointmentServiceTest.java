package org.example.project.service;

import org.example.project.exception.ConflictException;
import org.example.project.model.dto.request.AppointmentRequest;
import org.example.project.model.dto.response.AppointmentResponse;
import org.example.project.model.entity.Appointment;
import org.example.project.model.entity.User;
import org.example.project.model.enums.AppointmentStatus;
import org.example.project.model.enums.Role;
import org.example.project.repository.AppointmentRepository;
import org.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User mockPatient() {
        return User.builder()
                .id(1L)
                .username("patient1")
                .role(Role.PATIENT)
                .isActive(true)
                .build();
    }

    private User mockDoctor() {
        return User.builder()
                .id(2L)
                .username("doctor1")
                .role(Role.DOCTOR)
                .isActive(true)
                .build();
    }

    private Appointment mockAppointment() {
        return Appointment.builder()
                .id(1L)
                .patient(mockPatient())
                .doctor(mockDoctor())
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(AppointmentStatus.PENDING)
                .build();
    }

    @Test
    void bookAppointment_Success() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(2L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockDoctor()));
        when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(mockAppointment());

        AppointmentResponse result = appointmentService.bookAppointment(1L, request);

        assertNotNull(result);
        assertEquals(AppointmentStatus.PENDING, result.getStatus());
    }

    @Test
    void bookAppointment_OverlappingSlot_ThrowsConflict() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(2L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockDoctor()));
        when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(1L, request));
    }

    @Test
    void bookAppointment_SamePatientAndDoctor_ThrowsConflict() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient()));

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(1L, request));
    }

    @Test
    void approveAppointment_Success() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment()));
        when(appointmentRepository.save(any())).thenReturn(mockAppointment());

        AppointmentResponse result = appointmentService.approveAppointment(1L, 2L);

        assertNotNull(result);
    }

    @Test
    void approveAppointment_WrongDoctor_ThrowsAccessDenied() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment()));

        assertThrows(AccessDeniedException.class, () -> appointmentService.approveAppointment(1L, 99L));
    }

    @Test
    void cancelAppointment_Success() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment()));
        when(appointmentRepository.save(any())).thenReturn(mockAppointment());

        AppointmentResponse result = appointmentService.cancelAppointment(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void cancelAppointment_NotOwner_ThrowsAccessDenied() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment()));

        assertThrows(AccessDeniedException.class, () -> appointmentService.cancelAppointment(1L, 99L));
    }

    @Test
    void bookAppointment_EndTimeBeforeStartTime_ThrowsConflict() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(2L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(9, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockDoctor()));

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(1L, request));
    }

    @Test
    void bookAppointment_LessThan15Minutes_ThrowsConflict() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(2L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 10));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockDoctor()));

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(1L, request));
    }

    @Test
    void rejectAppointment_NotPending_ThrowsConflict() {
        Appointment approved = mockAppointment();
        approved.setStatus(AppointmentStatus.APPROVED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(approved));

        assertThrows(ConflictException.class, () -> appointmentService.rejectAppointment(1L, 2L));
    }
}
