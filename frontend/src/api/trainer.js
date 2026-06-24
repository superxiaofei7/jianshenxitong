import request from './request'

export function getTrainerList(params) {
  return request.get('/trainer/list', { params })
}

export function getAvailableTrainers(params) {
  return request.get('/trainer/available', { params })
}

export function getTrainerById(id) {
  return request.get(`/trainer/${id}`)
}

export function getTrainerByUserId(userId) {
  return request.get(`/trainer/byUser/${userId}`)
}

export function saveTrainer(data) {
  return request.post('/trainer', data)
}

export function updateTrainer(data) {
  return request.put('/trainer', data)
}

export function deleteTrainer(id) {
  return request.delete(`/trainer/${id}`)
}
