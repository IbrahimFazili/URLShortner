<template>
  <div class="bg-white shadow-lg rounded-sm border border-gray-200">
    <header class="px-5 py-4 border-b border-gray-100">
      <div class="flex items-start justify-between">
        <div>
          <h2 class="font-semibold text-gray-800">Network</h2>
          <h3 class="text-xs text-secondary-500">
            Replication Factor: {{ replicationFactor }}
          </h3>
        </div>
        <Btn
          type="submit"
          variant="primary"
          size="sm"
          outline
          @click="$emit('refresh')"
        >
          <ArrowPathIcon class="w-6" />
        </Btn>
      </div>
    </header>
    <div class="p-3">
      <!-- Table -->
      <div class="overflow-auto">
        <table class="table-auto w-full">
          <!-- Table header -->
          <thead class="text-xs uppercase text-gray-400 bg-gray-50 rounded-sm">
            <tr>
              <th class="p-2">
                <div class="font-semibold text-left">Address</div>
              </th>
              <th class="p-2">
                <div class="font-semibold text-center">ID</div>
              </th>
              <th class="p-2">
                <div class="font-semibold text-center">State</div>
              </th>
            </tr>
          </thead>
          <!-- Table body -->
          <tbody class="text-sm font-medium divide-y divide-gray-100">
            <!-- Row -->
            <tr v-for="node in network">
              <td class="p-2">
                <div class="flex items-center">
                  <ComputerDesktopIcon class="w-5 ml-2 mr-4 sm:mr-5" />
                  <div class="text-gray-800">{{ node.Addr }}</div>
                </div>
              </td>
              <td class="p-2">
                <div class="text-center">{{ node.ID }}</div>
              </td>
              <td class="p-2">
                <div
                  class="text-center uppercase"
                  :class="{
                    'text-secondary-500': node.State === 0,
                    'text-primary-500': node.State === 1,
                    'text-warning-500': node.State === 2,
                    'text-danger-500': node.State === 3,
                  }"
                >
                  {{
                    node.State === 0
                      ? 'starting'
                      : node.State === 1
                      ? 'ok'
                      : node.State === 2
                      ? 'unreachable'
                      : 'dead'
                  }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ComputerDesktopIcon, ArrowPathIcon } from '@heroicons/vue/24/outline'
import Btn from '../../components/Btn.vue'

defineProps(['network', 'replicationFactor'])
const emit = defineEmits(['refresh'])
</script>
