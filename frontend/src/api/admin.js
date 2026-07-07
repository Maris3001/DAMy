import http from './http'

// ===== Admin CRUD (yêu cầu ROLE_ADMIN) =====

// --- Phim ---
export function adminListMovies(params) {
  return http.get('/admin/movies', { params }).then((res) => res.data)
}
export function adminCreateMovie(payload) {
  return http.post('/admin/movies', payload).then((res) => res.data)
}
export function adminUpdateMovie(id, payload) {
  return http.put(`/admin/movies/${id}`, payload).then((res) => res.data)
}
export function adminDeleteMovie(id) {
  return http.delete(`/admin/movies/${id}`).then((res) => res.data)
}

// --- Suất chiếu ---
export function adminListShowtimes(params) {
  return http.get('/admin/showtimes', { params }).then((res) => res.data)
}
export function adminCreateShowtime(payload) {
  return http.post('/admin/showtimes', payload).then((res) => res.data)
}
export function adminUpdateShowtime(id, payload) {
  return http.put(`/admin/showtimes/${id}`, payload).then((res) => res.data)
}
export function adminDeleteShowtime(id) {
  return http.delete(`/admin/showtimes/${id}`).then((res) => res.data)
}

// --- Dữ liệu tham chiếu cho form ---
export function adminListRegions() {
  return http.get('/admin/regions').then((res) => res.data)
}
export function adminListChains() {
  return http.get('/admin/cinema-chains').then((res) => res.data)
}

// --- Rạp ---
export function adminListCinemas(regionId) {
  return http.get('/admin/cinemas', { params: { regionId } }).then((res) => res.data)
}
export function adminCreateCinema(payload) {
  return http.post('/admin/cinemas', payload).then((res) => res.data)
}
export function adminUpdateCinema(id, payload) {
  return http.put(`/admin/cinemas/${id}`, payload).then((res) => res.data)
}
export function adminDeleteCinema(id) {
  return http.delete(`/admin/cinemas/${id}`).then((res) => res.data)
}

// --- Phòng chiếu ---
export function adminListRooms(cinemaId) {
  return http.get('/admin/rooms', { params: { cinemaId } }).then((res) => res.data)
}
export function adminCreateRoom(payload) {
  return http.post('/admin/rooms', payload).then((res) => res.data)
}
export function adminUpdateRoom(id, payload) {
  return http.put(`/admin/rooms/${id}`, payload).then((res) => res.data)
}
export function adminDeleteRoom(id) {
  return http.delete(`/admin/rooms/${id}`).then((res) => res.data)
}

// --- Ghế ---
export function adminListSeats(roomId) {
  return http.get(`/admin/rooms/${roomId}/seats`).then((res) => res.data)
}
export function adminGenerateSeats(roomId, payload) {
  return http.post(`/admin/rooms/${roomId}/seats/generate`, payload).then((res) => res.data)
}
export function adminUpdateSeats(roomId, payload) {
  return http.put(`/admin/rooms/${roomId}/seats`, payload).then((res) => res.data)
}

// --- Bắp nước ---
export function adminListConcessions() {
  return http.get('/admin/concessions').then((res) => res.data)
}
export function adminCreateConcession(payload) {
  return http.post('/admin/concessions', payload).then((res) => res.data)
}
export function adminUpdateConcession(id, payload) {
  return http.put(`/admin/concessions/${id}`, payload).then((res) => res.data)
}
export function adminDeleteConcession(id) {
  return http.delete(`/admin/concessions/${id}`).then((res) => res.data)
}

// --- Thể loại phim ---
export function adminListGenres() {
  return http.get('/admin/genres').then((res) => res.data)
}
export function adminCreateGenre(payload) {
  return http.post('/admin/genres', payload).then((res) => res.data)
}
export function adminUpdateGenre(id, payload) {
  return http.put(`/admin/genres/${id}`, payload).then((res) => res.data)
}
export function adminDeleteGenre(id) {
  return http.delete(`/admin/genres/${id}`).then((res) => res.data)
}

// --- Upload ảnh ---
export function adminUploadImage(file) {
  const form = new FormData()
  form.append('file', file)
  return http
    .post('/admin/uploads', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((res) => res.data)
}
