<template>
  <div class="px-4 sm:px-6 lg:px-8 py-8 w-full max-w-9xl mx-auto">
    <div class="grid xl:grid-cols-2 gap-6">
      <ShortenerActions @put="putUrl" />
      <div v-html="putHTML"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'
import { ShortenerActions } from '../partials/shortener'

const toast = useToast()
const putHTML = ref('')

const putUrl = async (shortUrl, longUrl) => {
  const res = await axios
    .put(import.meta.env.VITE_LOAD_BALANCER, null, {
      params: {
        short: shortUrl,
        long: longUrl,
      },
    })
    .catch((err) => {
      console.error(err)
    })

  if (!res?.data) {
    toast.error('Error while saving URL')
  }

  putHTML.value = res.data
}
</script>
