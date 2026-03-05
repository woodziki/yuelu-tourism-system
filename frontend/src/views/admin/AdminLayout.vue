<template>
  <div class="admin-layout">
    <el-container class="admin-container">
      <!-- 左侧侧边栏 -->
      <el-aside width="200px" class="admin-aside">
        <div class="admin-logo">
          <span class="logo-text">YL Admin</span>
        </div>
        <el-menu
          :default-active="$route.path"
          class="admin-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/admin/spots">
            <i class="el-icon-location-outline"></i>
            <span slot="title">📍 景点管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <i class="el-icon-chat-dot-square"></i>
            <span slot="title">💬 评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <i class="el-icon-user"></i>
            <span slot="title">👥 用户管理</span>
          </el-menu-item>
          <el-menu-item index="/">
            <i class="el-icon-s-home"></i>
            <span slot="title">🏠 返回前台主页</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧主区域 -->
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <h1 class="header-title">岳麓山后台管理中心</h1>
          </div>
          <div class="header-right">
            <el-avatar
              size="small"
              icon="el-icon-user-solid"
              class="admin-avatar"
            />
            <span class="admin-name">超级管理员</span>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout'
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: #f0f2f5;
}

.admin-container {
  height: 100%;
}

/* 侧边栏样式 */
.admin-aside {
  background-color: #304156;
  color: #bfcbd9;
  display: flex;
  flex-direction: column;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-weight: 600;
  font-size: 16px;
  letter-spacing: 1px;
  border-bottom: 1px solid #263445;
}

.logo-text {
  white-space: nowrap;
}

.admin-menu {
  border-right: none;
}

/* 顶部 Header */
.admin-header {
  height: 60px;
  line-height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 24px;
  box-sizing: border-box;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2933;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-avatar {
  margin-right: 8px;
}

.admin-name {
  font-size: 14px;
  color: #4b5563;
}

/* 主内容区域 */
.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
  box-sizing: border-box;
}
</style>