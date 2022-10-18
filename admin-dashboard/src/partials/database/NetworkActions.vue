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
            Add node
          </header>
          <li
            class="px-2 grow flex items-center border-b border-gray-100 text-sm py-2"
          >
            <div class="grow flex items-center">
              <div class="w-1/2">
                <input
                  v-model="newNodeUrl"
                  type="text"
                  class="text-sm"
                  placeholder="New node url"
                />
              </div>

              <div class="shrink-0 ml-2">
                <Btn
                  variant="primary"
                  outline
                  @click="emit('addNode', newNodeUrl)"
                  >Add</Btn
                >
              </div>
            </div>
          </li>
          <!-- Remove -->
          <header
            class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm font-semibold p-2"
          >
            Kill node
          </header>
          <li
            class="px-2 grow flex items-center border-b border-gray-100 text-sm py-2"
          >
            <div class="grow flex items-center">
              <div class="w-1/2">
                <input
                  v-model="existingNodeId"
                  class="text-sm"
                  type="number"
                  min="0"
                  placeholder="Node id"
                />
              </div>

              <div class="shrink-0 ml-2">
                <Btn
                  variant="primary"
                  outline
                  @click="emit('killNode', existingNodeId)"
                  >Kill</Btn
                >
              </div>
            </div>
          </li>
          <!-- Update replication factor -->
          <header
            class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm font-semibold p-2"
          >
            Update replication factor
          </header>
          <li
            class="px-2 grow flex items-center border-b border-gray-100 text-sm py-2"
          >
            <div class="grow flex items-center">
              <div class="w-1/2">
                <input
                  v-model="newReplicationFactor"
                  type="number"
                  class="text-sm"
                  placeholder="Enter a new replication factor"
                  min="0"
                />
              </div>

              <div class="shrink-0 ml-2">
                <Btn
                  variant="primary"
                  outline
                  @click="emit('updateReplicationFactor', newReplicationFactor)"
                  >Submit</Btn
                >
              </div>
            </div>
          </li>

          <!-- View Data -->
          <header
            class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm font-semibold p-2"
          >
            Data
          </header>
          <li class="px-2 grow border-b border-gray-100 text-sm py-2">
            <div>
              <Btn variant="primary" outline @click="emit('viewData')"
                >View Data</Btn
              >
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

const newNodeUrl = ref('')
const existingNodeId = ref('')
const newReplicationFactor = ref('')

const emit = defineEmits([
  'addNode',
  'killNode',
  'updateReplicationFactor',
  'viewData',
])
</script>
