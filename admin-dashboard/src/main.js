import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Toast, { POSITION } from 'vue-toastification'
import router from './router'
import App from './App.vue'

import 'vue-toastification/dist/index.css'
import './css/style.css'

const toastOptions = {
  position: POSITION.TOP_RIGHT,
  hideProgressBar: true,
}

const pinia = createPinia()
const app = createApp(App)
app.use(router)
app.use(pinia)
app.use(Toast, toastOptions)
app.mount('#app')
