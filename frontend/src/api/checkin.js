import request from './request'

export function doCheckIn(bookingId) {
  return request.post(`/checkin/${bookingId}`)
}

export function getMemberCheckIns(memberId, params) {
  return request.get(`/checkin/member/${memberId}`, { params })
}

export function getTrainerCheckIns(trainerId, params) {
  return request.get(`/checkin/trainer/${trainerId}`, { params })
}

export function getAllCheckIns(params) {
  return request.get('/checkin/list', { params })
}
