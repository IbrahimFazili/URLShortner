<template>
  <div class="relative inline-flex">
    <button
      v-if="store.isLoggedIn"
      ref="trigger"
      class="inline-flex justify-center items-center group"
      aria-haspopup="true"
      @click.prevent="dropdownOpen = !dropdownOpen"
      :aria-expanded="dropdownOpen"
    >
      <UserIcon class="w-6 rounded-full" />
      <div class="flex items-center truncate">
        <span
          class="truncate ml-2 text-sm font-medium group-hover:text-gray-800"
          >Admin</span
        >
      </div>
    </button>
    <Btn v-else :to="{ name: 'login' }" variant="primary" size="sm">Login</Btn>

    <transition
      enter-active-class="transition ease-out duration-200 transform"
      enter-from-class="opacity-0 -translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-out duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-show="dropdownOpen"
        class="origin-top-right z-10 absolute top-full min-w-44 bg-white border border-gray-200 py-1.5 rounded shadow-lg overflow-hidden mt-1"
        :class="align === 'right' ? 'right-0' : 'left-0'"
      >
        <div class="pt-0.5 pb-2 px-3 mb-1 border-b border-gray-200">
          <div class="font-medium text-gray-800">Admin</div>
        </div>
        <ul
          ref="dropdown"
          @focusin="dropdownOpen = true"
          @focusout="dropdownOpen = false"
        >
          <li>
            <router-link
              class="font-medium text-sm text-indigo-500 hover:text-indigo-600 flex items-center py-1 px-3"
              :to="{ name: 'logout' }"
              @click="dropdownOpen = false"
              >Sign Out
            </router-link>
          </li>
        </ul>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { UserIcon } from '@heroicons/vue/24/solid'
import { useStore } from '../../store'
import { Btn } from '../components'

defineProps(['align'])

const store = useStore()

const dropdownOpen = ref(false)
const trigger = ref(null)
const dropdown = ref(null)

// close on click outside
const clickHandler = ({ target }) => {
  if (
    !dropdownOpen.value ||
    dropdown.value.contains(target) ||
    trigger.value.contains(target)
  )
    return
  dropdownOpen.value = false
}

// close if the esc key is pressed
const keyHandler = ({ keyCode }) => {
  if (!dropdownOpen.value || keyCode !== 27) return
  dropdownOpen.value = false
}

onMounted(() => {
  document.addEventListener('click', clickHandler)
  document.addEventListener('keydown', keyHandler)
})

onUnmounted(() => {
  document.removeEventListener('click', clickHandler)
  document.removeEventListener('keydown', keyHandler)
})
</script>
