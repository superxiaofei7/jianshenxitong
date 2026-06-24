import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = () => !!token.value

  const hasRole = (role) => {
    return user.value && user.value.role === role
  }

  const isAdmin = () => {
    return user.value && user.value.role === 'FRONT_DESK'
  }

  const isFinance = () => {
    return user.value && user.value.role === 'FINANCE'
  }

  const isTrainer = () => {
    return user.value && user.value.role === 'TRAINER'
  }

  const isMember = () => {
    return user.value && user.value.role === 'MEMBER'
  }

  async function login(username, password) {
    const res = await loginApi(username, password)
    if (res.data.code === 200) {
      token.value = res.data.data.token
      user.value = res.data.data.user
      localStorage.setItem('token', token.value)
      localStorage.setItem('user', JSON.stringify(user.value))
    }
    return res.data
  }

  function updateUser(newUser) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, hasRole, isAdmin, isFinance, isTrainer, isMember, login, updateUser, logout }
})
