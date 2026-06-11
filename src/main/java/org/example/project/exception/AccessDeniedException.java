package org.example.project.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super("Không có quyền thực hiện thao tác này");
    }
}
