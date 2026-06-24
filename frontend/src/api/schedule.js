import request from './request'

export function getTrainerSchedule(trainerId, date) {
  return request.get(`/schedule/trainer/${trainerId}`, { params: { date } })
}

export function getAvailableSlots(trainerId, startDate, endDate) {
  return request.get(`/schedule/available/${trainerId}`, { params: { startDate, endDate } })
}

export function setSchedule(trainerId, date, data) {
  return request.post(`/schedule/set/${trainerId}`, data, { params: { date } })
}

export function checkConflict(params) {
  return request.get('/schedule/checkConflict', { params })
}

// 教练自己的排班接口
export function getMySchedule(date) {
  return request.get('/schedule/mySchedule', { params: { date } })
}

export function setMySchedule(date, data) {
  return request.post('/schedule/mySchedule', data, { params: { date } })
}
