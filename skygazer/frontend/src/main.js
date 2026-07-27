// 应用入口：创建 Vue 应用实例并挂载
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import '@fontsource/sora/400.css'
import '@fontsource/sora/500.css'
import '@fontsource/sora/600.css'
import '@fontsource/sora/700.css'
import '@fontsource/sora/800.css'
import './styles/main.css'

// 全局单例：Pinia 负责跨组件状态管理（用户、主题等），router 负责页面路由
const app = createApp(App)

app.use(createPinia())
app.use(router)

// 挂载到 index.html 中的 #app 节点，启动应用
app.mount('#app')
