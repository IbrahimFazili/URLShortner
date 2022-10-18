<template>
  <div class="bg-white shadow-lg rounded-sm border border-gray-200">
    <header class="px-5 py-4 border-b border-gray-100">
      <h2 class="font-semibold text-gray-800">Actions</h2>
    </header>
    <div class="p-3">
      <!-- Card content -->
      <div>
        <ul class="my-1">
          <!-- Add -->
          <header
            class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm font-semibold p-2"
          >
            Add host
          </header>
          <li
            class="px-2 grow flex items-center border-b border-gray-100 text-sm py-2"
          >
            <div class="grow flex items-center">
              <div class="w-1/2">
                <input
                  v-model="newHost"
                  type="text"
                  class="text-sm"
                  placeholder="New host name"
                />
              </div>

              <div class="shrink-0 ml-2">
                <Btn variant="primary" outline @click="emit('addHost', newHost)"
                  >Add</Btn
                >
              </div>
            </div>
          </li>
          <!-- Remove -->
          <header
            class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm font-semibold p-2"
          >
            Kill host
          </header>
          <li
            class="px-2 grow flex items-center border-b border-gray-100 text-sm py-2"
          >
            <div class="grow flex items-center">
              <div class="w-1/2">
                <input
                  v-model="existingHost"
                  type="text"
                  class="text-sm"
                  placeholder="Host name"
                />
              </div>

              <div class="shrink-0 ml-2">
                <Btn
                  variant="primary"
                  outline
                  @click="emit('killHost', existingHost)"
                  >Kill</Btn
                >
              </div>
            </div>
          </li>
          <!-- Set -->
          <header
            class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm font-semibold p-2"
          >
            Set hosts
          </header>
          <li
            class="px-2 grow flex items-center border-b border-gray-100 text-sm py-2"
          >
            <div class="grow flex items-center">
              <div class="w-1/2">
                <input
                  v-model="minHosts"
                  type="text"
                  class="text-sm"
                  placeholder="Minimum number of hosts"
                />
              </div>

              <div class="shrink-0 ml-2">
                <Btn
                  variant="primary"
                  outline
                  @click="emit('setHosts', minHosts)"
                  >Set</Btn
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

const newHost = ref('')
const existingHost = ref('')
const minHosts = ref('')

const emit = defineEmits(['addHost', 'killHost', 'setHosts'])
</script>
