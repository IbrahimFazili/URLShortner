<template>
  <div class="px-5 py-3">
    <div>
      <div class="text-3xl font-bold text-gray-800 mr-2 tabular-nums">
        <span ref="chartValue">57.81</span>
      </div>
      <div class="text-xs text-secondary-500">{{ tag }}</div>
    </div>
  </div>
  <div class="grow">
    <canvas ref="canvas" :data="data" :width="width" :height="height"></canvas>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  Chart,
  Filler,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Tooltip,
} from 'chart.js'
import 'chartjs-adapter-moment'

Chart.register(
  Filler,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Tooltip
)

export default {
  name: 'RealtimeChart',
  props: ['data', 'tag', 'width', 'height'],
  setup(props) {
    const canvas = ref(null)
    const chartValue = ref(null)
    let chart = null

    // function that updates header values
    const handleHeaderValues = (data, chartValue) => {
      chartValue.value.innerHTML = data.datasets[0].data.length
        ? data.datasets[0].data[data.datasets[0].data.length - 1].y
        : 0
    }

    onMounted(() => {
      const ctx = canvas.value
      chart = new Chart(ctx, {
        type: 'line',
        data: props.data,
        options: {
          layout: {
            padding: 20,
          },
          scales: {
            y: {
              grid: {
                drawBorder: false,
              },
              suggestedMin: 0,
              suggestedMax: 100,
              ticks: {
                maxTicksLimit: 5,
              },
            },
            x: {
              type: 'time',
              time: {
                parser: 'hh:mm:ss',
                unit: 'second',
                tooltipFormat: 'MMM DD, H:mm:ss a',
                displayFormats: {
                  second: 'H:mm:ss',
                },
              },
              grid: {
                display: false,
                drawBorder: false,
              },
              ticks: {
                autoSkipPadding: 96,
                maxRotation: 0,
              },
            },
          },
          plugins: {
            legend: {
              display: false,
            },
            tooltip: {
              titleFont: {
                weight: '600',
              },
            },
          },
          interaction: {
            intersect: false,
            mode: 'nearest',
          },
          animation: false,
          maintainAspectRatio: false,
          resizeDelay: 200,
        },
      })
      // output header values
      handleHeaderValues(props.data, chartValue)
    })

    onUnmounted(() => chart.destroy())

    watch(
      () => props.data,
      (data) => {
        // update chart
        chart.data = data
        handleHeaderValues(data, chartValue)
        chart.update()
      }
    )

    return {
      canvas,
      chartValue,
    }
  },
}
</script>
