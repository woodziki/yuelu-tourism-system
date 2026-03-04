// src/router/index.js
import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import SpotDetail from '@/views/SpotDetail.vue'
import MyFavorites from '@/views/MyFavorites.vue'
import AllSpots from '@/views/AllSpots.vue'

Vue.use(Router)

const router = new Router({
  mode: 'hash',
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/all',
      name: 'AllSpots',
      component: AllSpots
    },
    {
      path: '/favorites',
      name: 'MyFavorites',
      component: MyFavorites
    },
    {
      // 冒号代表动态参数，:id 会接收具体的景点编号
      path: '/spot/:id',
      name: 'SpotDetail',
      component: SpotDetail
    },
    {
      path: '*',
      redirect: '/'
    }
  ]
})

export default router