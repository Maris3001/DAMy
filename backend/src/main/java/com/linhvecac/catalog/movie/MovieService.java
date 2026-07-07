package com.linhvecac.catalog.movie;

import com.linhvecac.catalog.movie.dto.MovieRequest;
import com.linhvecac.catalog.movie.dto.MovieResponse;
import com.linhvecac.common.ApiException;
import com.linhvecac.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<MovieResponse> listPublic(MovieStatus status) {
        List<Movie> movies = (status != null)
                ? movieRepository.findByStatusOrderByReleaseDateDesc(status)
                : movieRepository.findByStatusInOrderByReleaseDateDesc(
                        List.of(MovieStatus.NOW_SHOWING, MovieStatus.COMING_SOON));
        return movies.stream().map(MovieResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MovieResponse get(Long id) {
        return MovieResponse.from(getMovie(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<MovieResponse> adminList(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Movie> result = (search != null && !search.isBlank())
                ? movieRepository.findByTitleContainingIgnoreCase(search.trim(), pageable)
                : movieRepository.findAll(pageable);
        return PageResponse.of(result, MovieResponse::from);
    }

    @Transactional
    public MovieResponse create(MovieRequest request) {
        Movie movie = new Movie();
        apply(movie, request);
        return MovieResponse.from(movieRepository.save(movie));
    }

    @Transactional
    public MovieResponse update(Long id, MovieRequest request) {
        Movie movie = getMovie(id);
        apply(movie, request);
        return MovieResponse.from(movieRepository.save(movie));
    }

    @Transactional
    public void delete(Long id) {
        Movie movie = getMovie(id);
        try {
            movieRepository.delete(movie);
            movieRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Không thể xóa phim đang có suất chiếu. Vui lòng xóa suất chiếu trước.");
        }
    }

    private void apply(Movie movie, MovieRequest r) {
        movie.setTitle(r.title().trim());
        movie.setDescription(r.description());
        movie.setDurationMin(r.durationMin());
        movie.setGenres(resolveGenres(r.genreIds()));
        movie.setAgeRating(r.ageRating() != null ? r.ageRating() : "P");
        movie.setPosterUrl(r.posterUrl());
        movie.setBackdropUrl(r.backdropUrl());
        movie.setTrailerUrl(r.trailerUrl());
        movie.setStatus(r.status());
        movie.setReleaseDate(r.releaseDate());
    }

    private List<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = genreIds.stream().distinct().toList();
        List<Genre> genres = genreRepository.findAllById(ids);
        if (genres.size() != ids.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Thể loại không hợp lệ");
        }
        return genres;
    }

    private Movie getMovie(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy phim"));
    }
}
