<template>
  <div class="px-4 sm:px-6 lg:px-8 py-8 w-full max-w-9xl mx-auto">
    <div v-if="isDatabaseUp" class="grid xl:grid-cols-2 gap-6">
      <NetworkChart
        :network="dbNetwork"
        :replicationFactor="dbReplicationFactor"
        @refresh="getNetwork"
      />
      <NetworkActions
        @add-node="addNode"
        @kill-node="killNode"
        @update-replication-factor="updateReplicationFactor"
        @view-data="viewData"
      />
    </div>
    <div class="flex" v-else>
      <div class="bg-danger-500 w-2 h-12 rounded-l"></div>

      <div class="flex-1 bg-danger-300 text-sm px-2 py-3 rounded-r">
        Cannot connect to the database
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'
import { NetworkChart, NetworkActions } from '../partials/database'
import router from '../router'

const isDatabaseUp = ref(true)
const dbNetwork = ref([])
const dbReplicationFactor = ref(0)

const toast = useToast()

const getNetwork = async () => {
  const res = await axios
    .get(import.meta.env.VITE_DB_CONTROLLER + '/network')
    .catch((err) => {
      console.error(err)
    })

  if (!res?.data) {
    isDatabaseUp.value = false
    console.error('Error fetching network')
    return
  }

  dbNetwork.value = res.data.Nodes
  dbReplicationFactor.value = res.data.ReplicationFactor
}

const addNode = async (nodeUrl) => {
  const res = await axios
    .post(import.meta.env.VITE_DB_CONTROLLER + '/addnode', null, {
      params: {
        nodeurl: nodeUrl,
      },
    })
    .catch((err) => {
      console.error(err)
    })

  if (res?.status == 201) {
    toast.success('Adding node to network')
  } else {
    toast.error('Error adding node to network')
  }

  getNetwork()
}

const killNode = async (nodeId) => {
  const res = await axios
    .patch(import.meta.env.VITE_DB_CONTROLLER + '/killnode', null, {
      params: {
        nodeID: nodeId,
      },
    })
    .catch((err) => {
      console.error(err)
    })

  if (res?.status == 204) {
    toast.success('Removing node from network')
  } else {
    toast.error('Error removing node from network')
  }

  getNetwork()
}

const updateReplicationFactor = async (newReplicationFactor) => {
  const res = await axios
    .patch(import.meta.env.VITE_DB_CONTROLLER + '/rfupdate', null, {
      params: {
        rf: newReplicationFactor,
      },
    })
    .catch((err) => {
      console.error(err)
    })

  if (res?.status == 204) {
    toast.success('Updating replication factor')
  } else {
    toast.error('Error while updating replication factor')
  }

  getNetwork()
}

const viewData = async () => {
  const res = await axios
    .get(import.meta.env.VITE_DB_CONTROLLER + '/data')
    .catch((err) => {
      console.error(err)
    })

  if (!res?.data) {
    toast.error('Error while retrieving data')
    return
  }

  router.push({
    name: 'database-data',
    params: {
      data: `${res.data}`,
    },
  })
}

getNetwork()
</script>
