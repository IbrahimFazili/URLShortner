<template>
  <div class="px-4 sm:px-6 lg:px-8 py-8 w-full max-w-9xl mx-auto">
    <div v-if="isProxyUp" class="grid xl:grid-cols-2 gap-6">
      <HostChart :hosts="hosts" :min-hosts="minHosts" @refresh="getHosts" />
      <HostActions
        @add-host="addHost"
        @kill-host="killHost"
        @set-hosts="setHosts"
      />
      <RequestRateChart
        :data="requestRateData"
        width="595"
        height="248"
        @update="updateRequestRateChart"
      />
    </div>
    <div class="flex" v-else>
      <div class="bg-danger-500 w-2 h-12 rounded-l"></div>

      <div class="flex-1 bg-danger-300 text-sm px-2 py-3 rounded-r">
        Cannot connect to the proxy server
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'
import {
  HostActions,
  HostChart,
  RequestRateChart,
} from '../partials/loadBalancer'

const isProxyUp = ref(true)
const hosts = ref([])
const minHosts = ref(0)

const toast = useToast()

// Realtime chart
const MAX_REQUEST_DATA_LENGTH = 10
// Number of timestamps to requests
const requestRateData = ref([])

const healthCheck = async () => {
  const res = await axios
    .get(import.meta.env.VITE_LOAD_BALANCER_API + '/pinternal/healthcheck')
    .catch((err) => {
      console.error(err)
    })

  if (!res?.data) {
    isProxyUp.value = false
    return false
  }

  return res.status == 200
}

const getHosts = async () => {
  const res = await axios
    .get(import.meta.env.VITE_LOAD_BALANCER_API + '/pinternal/hosts')
    .catch((err) => {
      console.error(err)
    })

  if (!res?.data) {
    console.error('Error fetching hosts')
    return
  }

  hosts.value = parseHosts(res.data)
}

const getMinHosts = async () => {
  const res = await axios
    .get(import.meta.env.VITE_LOAD_BALANCER_API + '/pinternal/minhosts')
    .catch((err) => {
      console.error(err)
    })

  if (!res?.data) {
    console.error('Error fetching minimum number of hosts')
    return
  }

  minHosts.value = parseInt(res.data)
}

const parseHosts = (hosts) => {
  const hostArray = []
  const hostStrings = hosts.split(',')

  hostStrings.forEach((hostString) => {
    const hostStringArray = hostString.split('|')
    hostArray.push({
      name: hostStringArray[0].trim(),
      status: hostStringArray[1].trim(),
    })
  })

  return hostArray
}

const addHost = async (hostname) => {
  const res = await axios
    .post(import.meta.env.VITE_LOAD_BALANCER_API + '/pinternal/addhost', null, {
      params: {
        host: hostname,
      },
    })
    .catch((err) => {
      console.error(err)
    })

  if (res?.status == 200) {
    toast.success('Adding host to network')
  } else {
    toast.error('Error adding host to network')
  }

  getHosts()
}

const killHost = async (hostname) => {
  const res = await axios
    .post(
      import.meta.env.VITE_LOAD_BALANCER_API + '/pinternal/killhost',
      null,
      {
        params: {
          host: hostname,
        },
      }
    )
    .catch((err) => {
      console.error(err)
    })

  if (res?.status == 200) {
    toast.success('Removing host from network')
  } else {
    toast.error('Error removing host from network')
  }

  getHosts()
}

const setHosts = async (numHosts) => {
  const res = await axios
    .post(
      import.meta.env.VITE_LOAD_BALANCER_API + '/pinternal/sethosts',
      null,
      {
        params: {
          hosts: numHosts,
        },
      }
    )
    .catch((err) => {
      console.error(err)
    })

  if (!res?.status == 200) {
    toast.error('Error setting minimum number of hosts')
    return
  }

  toast.success('Updating minimum number of hosts')
  minHosts.value = numHosts
}

const updateRequestRateChart = async () => {
  const requestRate = await getRequestRate()
  requestRateData.value.push({
    x: new Date().toLocaleTimeString(),
    y: requestRate,
  })

  if (requestRateData.value.length > MAX_REQUEST_DATA_LENGTH) {
    requestRateData.value.shift()
  }
}

const getRequestRate = async () => {
  const res = await axios
    .get(import.meta.env.VITE_LOAD_BALANCER_API + '/pinternal/requestrate')
    .catch((err) => {
      console.error(err)
    })

  if (res?.status !== 200) {
    console.error('Error fetching request rate')
    return
  }

  return res.data
}

healthCheck()
getHosts()
getMinHosts()
</script>
