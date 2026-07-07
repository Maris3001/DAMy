package com.linhvecac.common;

import com.linhvecac.common.dto.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/** Upload ảnh (poster phim, ảnh bắp nước) — chỉ ADMIN (đường dẫn /api/admin/**). */
@RestController
@RequestMapping("/api/admin/uploads")
public class UploadController {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB

    private final Path uploadRoot;

    public UploadController(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping
    public UploadResponse upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vui lòng chọn tệp ảnh");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ảnh vượt quá 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận tệp ảnh");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        ext = (ext != null) ? ext.toLowerCase() : "";
        if (!ALLOWED.contains(ext)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Định dạng ảnh không hỗ trợ (jpg, png, webp, gif)");
        }

        String name = UUID.randomUUID() + "." + ext;
        try {
            Files.createDirectories(uploadRoot);
            Path target = uploadRoot.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Không lưu được ảnh, vui lòng thử lại");
        }
        return new UploadResponse("/api/files/" + name);
    }
}
