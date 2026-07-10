import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/main.css'
import './assets/css/animations.css'
import { animationOptimizer } from './utils/animationOptimizer'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.config.globalProperties.$animationOptimizer = animationOptimizer

app.mount('#app')
