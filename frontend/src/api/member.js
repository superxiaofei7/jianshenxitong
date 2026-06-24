import request from './request'

export function getMemberList(params) {
  return request.get('/member/list', { params })
}

export function getMemberById(id) {
  return request.get(`/member/${id}`)
}

export function saveMember(data) {
  return request.post('/member', data)
}

export function updateMember(data) {
  return request.put('/member', data)
}

export function deleteMember(id) {
  return request.delete(`/member/${id}`)
}

export function rechargeMember(id, params) {
  return request.post(`/member/${id}/recharge`, null, { params })
}

export function getMemberByUserId(userId) {
  return request.get(`/member/byUser/${userId}`)
}

export function getMemberStats(id) {
  return request.get(`/member/stats/${id}`)
}
