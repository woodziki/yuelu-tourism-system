// src/router/index.js

import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/views/Login.vue'

Vue.use(Router)

/**
 * 临时首页占位组件：
 *
 * <p>后续可替换为真正的首页视图（例如 Home.vue），用于展示“热门推荐 / 猜你喜欢”等核心功能。</p>
 */
const HomeView = {
  template:
    '<div style="padding: 40px; text-align: center; color: #334155;">首页占位（后续将展示岳麓山热门景点与个性化推荐）</div>'
}

/**
 * 路由基础配置：
 *
 * <ul>
 *   <li>/login：登录/注册一体化页面，对应 Login.vue；</li>
 *   <li>/：系统首页 / 推荐页（当前为占位组件）；</li>
 *   <li>*：任意未匹配路径统一重定向到首页，避免出现 404 空白页。</li>
 * </ul>
 */
const router = new Router({
  mode: 'hash', // 毕业设计演示环境推荐使用 hash，避免额外的后端配置
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      name: 'Home',
      component: HomeView
    },
    {
      path: '*',
      redirect: '/'
    }
  ]
})

export default router