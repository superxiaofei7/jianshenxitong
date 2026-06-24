import request from './request'

export function login(username, password) {
  return request.post('/login', { username, password })
}

export function register(data) {
  return request.post('/register', data)
}

export function updatePassword(oldPassword, newPassword) {
  return request.put('/password', null, { params: { oldPassword, newPassword } })
}

export function updateAvatar(avatar) {
  return request.put('/avatar', null, { params: { avatar } })
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function forgotPassword(data) {
  return request.post('/forgotPassword', data)
}

export function getProfile() {
  return request.get('/profile')
}

export function updateProfile(data) {
  return request.put('/profile', data)
}

export function rechargeBalance(amount, payMethod, remark) {
  return request.post('/member/recharge', null, { params: { amount, payMethod, remark } })
}
