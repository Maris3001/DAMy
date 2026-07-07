package com.linhvecac.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Phục vụ ảnh đã upload tại /api/files/** từ thư mục cấu hình app.upload.dir. */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public FileStorageConfig(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations(root.toUri().toString());
    }
}
