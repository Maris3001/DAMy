package com.linhvecac.catalog.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenreRequest(
        @NotBlank(message = "Vui lòng nhập tên thể loại")
        @Size(max = 60, message = "Tên thể loại tối đa 60 ký tự")
        String name) {
}
