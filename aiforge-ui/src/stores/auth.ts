import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string>(localStorage.getItem('aiforge_token') || '')

    const setToken = (newToken: string) => {
        token.value = newToken
        localStorage.setItem('aiforge_token', newToken)
    }

    const clearToken = () => {
        token.value = ''
        localStorage.removeItem('aiforge_token')
    }

    return { token, setToken, clearToken }
})