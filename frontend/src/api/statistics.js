import request from './request'

export function getDashboard() {
  return request.get('/statistics/dashboard')
}

export function getMonthlyRevenue(year, month) {
  return request.get('/statistics/revenue', { params: { year, month } })
}

export function getTrainerStats(trainerId) {
  return request.get(`/statistics/trainer/${trainerId}`)
}

export function getMemberActivity() {
  return request.get('/statistics/memberActivity')
}

export function getFinanceStats() {
  return request.get('/statistics/finance')
}
