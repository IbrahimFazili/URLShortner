import { defineStore } from 'pinia'
import { useStorage } from '@vueuse/core'

export const useStore = defineStore('main', {
  state: () => ({
    isLoggedIn: useStorage('isLoggedIn', false),
  }),

  actions: {
    login(email, _password) {
      if (email === 'admin@admin.com' && _password === 'test123') {
        this.isLoggedIn = true
      }
    },

    logout() {
      this.isLoggedIn = false
    },
  },
})
