<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useStore } from '../../store'
import { Btn } from '../components'
const router = useRouter()
const store = useStore()

const logout = async () => {
  store.logout()
  router.replace({ name: 'shortener' })
}
</script>

<template>
  <div class="mx-auto max-w-md rounded border p-5 text-center">
    <header class="mb-8">
      <h1 class="text-3xl">Sign out</h1>
    </header>

    <p class="mb-8">Are you sure you want to sign out?</p>

    <hr class="mb-8" />

    <Btn variant="danger" class="!block w-full" @click="logout">
      Yes, sign me out
    </Btn>
  </div>
</template>
