import request from './request'

export function createBooking(data) {
  return request.post('/booking', data)
}

export function cancelBooking(id) {
  return request.put(`/booking/${id}/cancel`)
}

export function confirmBooking(id) {
  return request.put(`/booking/${id}/confirm`)
}

export function getMemberBookings(memberId, params) {
  return request.get(`/booking/member/${memberId}`, { params })
}

export function getTrainerBookings(trainerId, params) {
  return request.get(`/booking/trainer/${trainerId}`, { params })
}

export function getAllBookings(params) {
  return request.get('/booking/list', { params })
}
