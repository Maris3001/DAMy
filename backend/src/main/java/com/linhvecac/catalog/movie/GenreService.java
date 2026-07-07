package com.linhvecac.catalog.movie;

import com.linhvecac.catalog.movie.dto.GenreRequest;
import com.linhvecac.catalog.movie.dto.GenreResponse;
import com.linhvecac.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<GenreResponse> list() {
        return genreRepository.findAllByOrderByNameAsc()
                .stream().map(GenreResponse::from).toList();
    }

    @Transactional
    public GenreResponse create(GenreRequest request) {
        Genre genre = new Genre();
        genre.setName(request.name().trim());
        return GenreResponse.from(save(genre));
    }

    @Transactional
    public GenreResponse update(Long id, GenreRequest request) {
        Genre genre = getGenre(id);
        genre.setName(request.name().trim());
        return GenreResponse.from(save(genre));
    }

    @Transactional
    public void delete(Long id) {
        Genre genre = getGenre(id);
        try {
            genreRepository.delete(genre);
            genreRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Không thể xóa thể loại đang được gán cho phim");
        }
    }

    private Genre save(Genre genre) {
        try {
            Genre saved = genreRepository.save(genre);
            genreRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, "Thể loại đã tồn tại");
        }
    }

    private Genre getGenre(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại"));
    }
}
