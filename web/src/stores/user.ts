import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserProfile } from '@/types/api'
import { fetchMe } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(null)

  function setToken(token: string) {
    localStorage.setItem('token', token)
  }

  function setProfile(data: UserProfile) {
    profile.value = data
  }

  async function loadProfile() {
    const res = await fetchMe()
    profile.value = res.data.data
    return profile.value
  }

  function clear() {
    profile.value = null
    localStorage.removeItem('token')
  }

  return { profile, setToken, setProfile, loadProfile, clear }
})
