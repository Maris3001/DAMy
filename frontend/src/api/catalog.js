import http from './http'

// ===== Public catalog (đọc, không cần đăng nhập) =====

export function listRegions() {
  return http.get('/regions').then((res) => res.data)
}

export function listRegionCinemas(regionId) {
  return http.get(`/regions/${regionId}/cinemas`).then((res) => res.data)
}

export function listCinemas(regionId) {
  return http.get('/cinemas', { params: { regionId } }).then((res) => res.data)
}

export function listConcessions() {
  return http.get('/concessions').then((res) => res.data)
}

/** Suất chiếu của rạp trong ngày (YYYY-MM-DD), gộp theo phim — bước 3 wizard. */
export function listCinemaShowtimes(cinemaId, date) {
  return http.get(`/cinemas/${cinemaId}/showtimes`, { params: { date } }).then((res) => res.data)
}
