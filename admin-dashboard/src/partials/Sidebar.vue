<template>
  <div>
    <!-- Sidebar backdrop (mobile only) -->
    <div
      class="fixed inset-0 bg-slate-900 bg-opacity-30 z-40 lg:hidden lg:z-auto transition-opacity duration-200"
      :class="sidebarOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'"
      aria-hidden="true"
    ></div>

    <!-- Sidebar -->
    <div
      id="sidebar"
      ref="sidebar"
      class="flex flex-col absolute z-40 left-0 top-0 lg:static lg:left-auto lg:top-auto lg:translate-x-0 h-screen overflow-y-scroll lg:overflow-y-auto no-scrollbar w-64 lg:w-20 lg:sidebar-expanded:!w-64 2xl:!w-64 shrink-0 bg-slate-800 p-4 transition-all duration-200 ease-in-out"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-64'"
    >
      <div class="space-y-8">
        <div>
          <h3 class="text-xs uppercase text-slate-500 font-semibold pl-3">
            <span
              class="hidden lg:block lg:sidebar-expanded:hidden 2xl:hidden text-center w-6"
              aria-hidden="true"
              >•••</span
            >
            <span class="lg:hidden lg:sidebar-expanded:block 2xl:block"
              >Pages</span
            >
          </h3>
          <ul class="mt-3">
            <!-- Shortener -->
            <router-link to="/" custom v-slot="{ href, navigate }">
              <li class="px-3 py-2 rounded-sm mb-0.5 last:mb-0">
                <a
                  class="block text-slate-200 hover:text-white truncate transition duration-150"
                  :href="href"
                  @click="navigate"
                >
                  <div class="flex items-center justify-between">
                    <div class="grow flex items-center">
                      <CloudIcon class="shrink-0 w-6" />
                      <span
                        class="text-sm font-medium ml-3 lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200"
                        >Shortener</span
                      >
                    </div>
                  </div>
                </a>
              </li>
            </router-link>
            <!-- Database -->
            <router-link
              v-if="store.isLoggedIn"
              to="/database"
              custom
              v-slot="{ href, navigate }"
            >
              <li class="px-3 py-2 rounded-sm mb-0.5 last:mb-0">
                <a
                  class="block text-slate-200 hover:text-white truncate transition duration-150"
                  :href="href"
                  @click="navigate"
                >
                  <div class="flex items-center">
                    <CircleStackIcon class="shrink-0 w-6" />
                    <span
                      class="text-sm font-medium ml-3 lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200"
                      >Database</span
                    >
                  </div>
                </a>
              </li>
            </router-link>
            <!-- Load Balancer -->
            <router-link
              v-if="store.isLoggedIn"
              to="/load-balancer"
              custom
              v-slot="{ href, navigate }"
            >
              <li class="px-3 py-2 rounded-sm mb-0.5 last:mb-0">
                <a
                  class="block text-slate-200 hover:text-white truncate transition duration-150"
                  :href="href"
                  @click="navigate"
                >
                  <div class="flex items-center">
                    <ServerStackIcon class="shrink-0 w-6" />
                    <span
                      class="text-sm font-medium ml-3 lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200"
                      >Load Balancer</span
                    >
                  </div>
                </a>
              </li>
            </router-link>
          </ul>
        </div>
      </div>

      <!-- Expand / collapse button -->
      <div class="pt-3 hidden lg:inline-flex 2xl:hidden justify-end mt-auto">
        <div class="px-3 py-2">
          <button @click.prevent="sidebarExpanded = !sidebarExpanded">
            <span class="sr-only">Expand / collapse sidebar</span>
            <svg
              class="w-6 h-6 fill-current sidebar-expanded:rotate-180"
              viewBox="0 0 24 24"
            >
              <path
                class="text-slate-400"
                d="M19.586 11l-5-5L16 4.586 23.414 12 16 19.414 14.586 18l5-5H7v-2z"
              />
              <path class="text-slate-600" d="M3 23H1V1h2z" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  CircleStackIcon,
  CloudIcon,
  ServerStackIcon,
} from '@heroicons/vue/24/outline'
import { useStore } from '../../store'

const props = defineProps(['sidebarOpen'])
const emit = defineEmits(['close-sidebar'])
const trigger = ref(null)
const sidebar = ref(null)
const storedSidebarExpanded = localStorage.getItem('sidebar-expanded')
const sidebarExpanded = ref(
  storedSidebarExpanded === null ? false : storedSidebarExpanded === 'true'
)

const store = useStore()

// close on click outside
const clickHandler = ({ target }) => {
  if (!sidebar.value || !trigger.value) return
  if (
    !props.sidebarOpen ||
    sidebar.value.contains(target) ||
    trigger.value.contains(target)
  )
    return
  emit('close-sidebar')
}

// close if the esc key is pressed
const keyHandler = ({ keyCode }) => {
  if (!props.sidebarOpen || keyCode !== 27) return
  emit('close-sidebar')
}

onMounted(() => {
  document.addEventListener('click', clickHandler)
  document.addEventListener('keydown', keyHandler)
})

onUnmounted(() => {
  document.removeEventListener('click', clickHandler)
  document.removeEventListener('keydown', keyHandler)
})

watch(sidebarExpanded, () => {
  localStorage.setItem('sidebar-expanded', sidebarExpanded.value)
  if (sidebarExpanded.value) {
    document.querySelector('body').classList.add('sidebar-expanded')
  } else {
    document.querySelector('body').classList.remove('sidebar-expanded')
  }
})
</script>
