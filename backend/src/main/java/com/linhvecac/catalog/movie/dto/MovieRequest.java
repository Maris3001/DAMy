package com.linhvecac.catalog.movie.dto;

import com.linhvecac.catalog.movie.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record MovieRequest(
        @NotBlank(message = "Vui lòng nhập tên phim")
        @Size(max = 200, message = "Tên phim tối đa 200 ký tự")
        String title,

        String description,

        @NotNull(message = "Vui lòng nhập thời lượng")
        @Positive(message = "Thời lượng phải lớn hơn 0")
        Integer durationMin,

        List<Long> genreIds,

        @Pattern(regexp = "P|K|T13|T16|T18", message = "Độ tuổi không hợp lệ")
        String ageRating,

        String posterUrl,
        String backdropUrl,
        String trailerUrl,

        @NotNull(message = "Vui lòng chọn trạng thái")
        MovieStatus status,

        LocalDate releaseDate) {
}
