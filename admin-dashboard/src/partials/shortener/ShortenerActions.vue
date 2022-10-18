<template>
  <div class="bg-white shadow-lg rounded-sm border border-gray-200">
    <header class="px-5 py-4 border-b border-gray-100">
      <h2 class="font-semibold text-gray-800">Actions</h2>
    </header>
    <div class="p-3">
      <!-- Card content -->
      <div>
        <ul class="my-1">
          <!-- PUT -->
          <header
            class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm font-semibold p-2"
          >
            PUT
          </header>
          <li
            class="px-2 grow flex items-center border-b border-gray-100 text-sm py-2"
          >
            <div class="grow flex items-center">
              <div class="w-1/2 space-y-2">
                <input
                  v-model="newShortUrl"
                  type="text"
                  class="text-sm"
                  placeholder="new short url"
                />

                <input
                  v-model="newLongUrl"
                  type="text"
                  class="text-sm"
                  placeholder="new long url"
                />
              </div>

              <div class="shrink-0 ml-2">
                <Btn
                  variant="primary"
                  outline
                  @click="emit('put', newShortUrl, newLongUrl)"
                  >PUT</Btn
                >
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Btn } from '../../components'

const newShortUrl = ref('')
const newLongUrl = ref('')

const emit = defineEmits(['get', 'put'])
</script>
