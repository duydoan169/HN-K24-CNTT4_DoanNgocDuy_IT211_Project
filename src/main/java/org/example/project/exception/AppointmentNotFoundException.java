package org.example.project.exception;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(Long id) {
        super("Không tìm thấy lịch khám với id: " + id);
    }
}
