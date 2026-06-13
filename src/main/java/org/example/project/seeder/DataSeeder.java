package org.example.project.seeder;

import lombok.RequiredArgsConstructor;
import org.example.project.model.entity.Appointment;
import org.example.project.model.entity.User;
import org.example.project.model.enums.AppointmentStatus;
import org.example.project.model.enums.Role;
import org.example.project.repository.AppointmentRepository;
import org.example.project.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;

        String password = passwordEncoder.encode("123456");

        User admin = userRepository.save(User.builder()
                .username("admin")
                .passwordHash(password)
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        User doctor1 = userRepository.save(User.builder()
                .username("doctor1")
                .passwordHash(password)
                .role(Role.DOCTOR)
                .isActive(true)
                .build());

        User doctor2 = userRepository.save(User.builder()
                .username("doctor2")
                .passwordHash(password)
                .role(Role.DOCTOR)
                .isActive(true)
                .build());

        User patient1 = userRepository.save(User.builder()
                .username("patient1")
                .passwordHash(password)
                .role(Role.PATIENT)
                .isActive(true)
                .build());

        User patient2 = userRepository.save(User.builder()
                .username("patient2")
                .passwordHash(password)
                .role(Role.PATIENT)
                .isActive(true)
                .build());

        User patient3 = userRepository.save(User.builder()
                .username("patient3")
                .passwordHash(password)
                .role(Role.PATIENT)
                .isActive(false)
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(patient1)
                .doctor(doctor1)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(AppointmentStatus.PENDING)
                .symptomDescription("Đau đầu, sổ mũi")
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(patient1)
                .doctor(doctor1)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.APPROVED)
                .symptomDescription("Đau bụng")
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(patient2)
                .doctor(doctor1)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(11, 30))
                .status(AppointmentStatus.PENDING)
                .symptomDescription("Khó thở")
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(patient2)
                .doctor(doctor2)
                .date(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .status(AppointmentStatus.CANCELLED)
                .symptomDescription("Đau lưng")
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(patient1)
                .doctor(doctor2)
                .date(LocalDate.now().plusDays(4))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(AppointmentStatus.COMPLETED)
                .symptomDescription("Kiểm tra sức khỏe định kỳ")
                .build());
    }
}
