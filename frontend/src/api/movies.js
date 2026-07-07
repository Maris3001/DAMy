import http from './http'

// ===== Public phim + suất chiếu =====

export function listMovies(status) {
  return http.get('/movies', { params: status ? { status } : {} }).then((res) => res.data)
}

export function getMovie(id) {
  return http.get(`/movies/${id}`).then((res) => res.data)
}

/** Suất chiếu của phim theo ngày (YYYY-MM-DD), gộp theo rạp. */
export function getMovieShowtimes(id, date) {
  return http.get(`/movies/${id}/showtimes`, { params: date ? { date } : {} }).then((res) => res.data)
}
