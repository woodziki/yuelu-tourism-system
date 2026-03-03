// src/main.js

import Vue from 'vue'
import App from './App.vue'
import router from './router'

// 引入 Element UI 及其主题样式
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

// 关闭生产环境提示，避免控制台出现不必要的信息
Vue.config.productionTip = false

// 全局注册 Element UI 组件库
Vue.use(ElementUI)

/**
 * 说明：
 * - axios 已在 src/utils/request.js 中封装，这里不直接挂载到 Vue 原型；
 * - 推荐做法是在 src/api 目录中按模块封装接口函数，组件只调用 API 层；
 * - 这样有利于保持“视图层 (Vue 组件) 与 数据访问层 (API) 解耦”，便于论文中展示架构设计。
 */

// 创建根 Vue 实例，并挂载到 index.html 中的 #app 节点
new Vue({
  router,
  render: h => h(App)
}).$mount('#app')