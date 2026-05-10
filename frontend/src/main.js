import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import './styles/element-variables.scss'
import './styles/global.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import formCreate from '@form-create/element-ui'
import FcDesigner from '@form-create/designer'

const app = createApp(App)

app.use(router)
app.use(pinia)
app.use(ElementPlus, {
    locale: zhCn,
})
app.use(formCreate)
app.use(FcDesigner)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.mount('#app')