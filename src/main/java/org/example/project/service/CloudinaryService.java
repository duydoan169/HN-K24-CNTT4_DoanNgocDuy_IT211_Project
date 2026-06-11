package org.example.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.example.project.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "application/pdf"
    );

    private static final long MAX_SIZE = 10 * 1024 * 1024;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ConflictException("File không được để trống");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ConflictException("Chỉ chấp nhận file JPG, PNG hoặc PDF");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ConflictException("File không được vượt quá 10MB");
        }

        try {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto")
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Tải file lên thất bại");
        }
    }
}
