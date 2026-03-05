// src/router/index.js
import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import SpotDetail from '@/views/SpotDetail.vue'
import MyFavorites from '@/views/MyFavorites.vue'
import AllSpots from '@/views/AllSpots.vue'
import Routes from '@/views/Routes.vue'

// Admin
import AdminLayout from '@/views/admin/AdminLayout.vue'
import SpotManage from '@/views/admin/SpotManage.vue'
import CommentManage from '@/views/admin/CommentManage.vue'
import UserManage from '@/views/admin/UserManage.vue'

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
      path: '/routes',
      name: 'Routes',
      component: Routes
    },
    {
      // 景点详情
      path: '/spot/:id',
      name: 'SpotDetail',
      component: SpotDetail
    },
    /**
     * 后台管理嵌套路由
     *
     * /admin          -> AdminLayout
     * /admin/spots    -> SpotManage  （景点管理）
     * /admin/comments -> CommentManage（评论管理）
     * /admin/users    -> UserManage  （用户管理）
     */
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/spots',
      children: [
        {
          path: 'spots',
          name: 'AdminSpots',
          component: SpotManage
        },
        {
          path: 'comments',
          name: 'AdminComments',
          component: CommentManage
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: UserManage
        }
      ]
    },
    {
      path: '*',
      redirect: '/'
    }
  ]
})

export default router