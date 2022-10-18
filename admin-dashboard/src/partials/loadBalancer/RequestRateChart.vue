<template>
  <div
    class="flex flex-col bg-white shadow-lg rounded-sm border border-gray-200"
  >
    <header class="px-5 py-4 border-b border-gray-100 flex items-center">
      <h2 class="font-semibold text-gray-800">Request Rate</h2>
    </header>

    <RealtimeChart
      :data="chartData"
      tag="requests per second"
      :width="width"
      :height="height"
    />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import RealtimeChart from '../../charts/RealtimeChart.vue'

// Import utilities
import { tailwindConfig, hexToRGB } from '../../utils/Utils'

const props = defineProps(['data', 'width', 'height'])
const emit = defineEmits(['update'])

// Fake update every 2 seconds
const interval = ref(null)
onMounted(() => {
  interval.value = setInterval(() => {
    emit('update')
  }, 2000)
})
onUnmounted(() => {
  clearInterval(interval)
})

const data = ref(props.data)

watch(props.data, (val) => {
  data.value = val
})

const chartData = computed(() => {
  return {
    datasets: [
      {
        // spread syntax is required to avoid "recursion" error
        data: [...data.value],
        fill: true,
        backgroundColor: `rgba(${hexToRGB(
          tailwindConfig().theme.colors.teal[500]
        )}, 0.08)`,
        borderColor: tailwindConfig().theme.colors.teal[500],
        borderWidth: 2,
        tension: 0,
        pointRadius: 0,
        pointHoverRadius: 3,
        pointBackgroundColor: tailwindConfig().theme.colors.teal[500],
        clip: 20,
      },
    ],
  }
})
</script>
