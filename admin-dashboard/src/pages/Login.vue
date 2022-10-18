<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from '../../store'
import { Btn } from '../components'

const router = useRouter()
const route = useRoute()
const store = useStore()
const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loginFailed = ref(false)

const login = () => {
  store.login(email.value, password.value)

  if (!store.isLoggedIn) {
    loginFailed.value = true
    return
  }
  // if supplied, redirect to the previous page after login
  const redirect = route.query.redirect
  if (redirect) {
    router.replace(redirect)
  } else {
    router.replace({ name: 'shortener' })
  }
}
</script>

<template>
  <div class="mx-auto max-w-md rounded border p-5">
    <header class="mb-8 text-center">
      <h1 class="text-3xl">Sign in</h1>
    </header>

    <form @submit.prevent="login">
      <div class="mb-5">
        <label for="email" class="sr-only">Email Address</label>
        <input
          id="email"
          v-model="email"
          type="email"
          placeholder="Email Address"
          required
          @input="loginFailed = false"
        />
      </div>

      <div class="mb-1">
        <label for="password" class="sr-only">Password</label>
        <input
          id="password"
          v-model="password"
          :type="showPassword ? 'text' : 'password'"
          placeholder="Password"
          required
          @input="loginFailed = false"
        />
      </div>

      <div class="mb-8 flex justify-between items-center">
        <div>
          <input
            id="show-password"
            v-model="showPassword"
            class="mr-2"
            type="checkbox"
          />

          <label for="show-password" class="text-sm">Show Password</label>
        </div>

        <div v-if="loginFailed" class="text-xs text-danger-500">
          Incorrect credentials supplied.
        </div>
      </div>

      <hr class="mb-8" />

      <Btn type="submit" variant="primary" class="!block w-full">Sign in</Btn>
    </form>
  </div>
</template>
