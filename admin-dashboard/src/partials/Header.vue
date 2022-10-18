<template>
  <header class="sticky top-0 bg-white border-b border-gray-200 z-30">
    <div class="px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16 -mb-px">
        <!-- Header: Left side -->
        <div class="flex">
          <!-- Hamburger button -->
          <button
            class="text-gray-500 hover:text-gray-600 lg:hidden"
            @click.stop="$emit('toggle-sidebar')"
            aria-controls="sidebar"
            :aria-expanded="sidebarOpen"
          >
            <span class="sr-only">Open sidebar</span>
            <Bars3Icon class="w-6" />
          </button>
        </div>

        <!-- Header: Right side -->
        <div class="flex items-center space-x-3">
          <UserMenu align="right" />
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import UserMenu from '../components/DropdownProfile.vue'
import { Bars3Icon } from '@heroicons/vue/24/outline'

defineProps(['sidebarOpen'])
</script>
