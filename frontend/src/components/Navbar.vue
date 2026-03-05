<template>
  <header class="navbar">
    <!-- 左侧 Logo 与标题 -->
    <div class="navbar-left">
      <div class="logo-circle">
        <span class="logo-text">YL</span>
      </div>
      <div class="title-wrapper">
        <h1 class="app-title">岳麓山景点推荐系统</h1>
        <p class="app-subtitle">基于协同过滤的个性化旅游助手</p>
      </div>
    </div>

    <!-- 中间导航菜单 -->
    <div class="navbar-center">
      <el-menu
        :default-active="activePath"
        mode="horizontal"
        :router="true"
        class="nav-menu"
        background-color="transparent"
        text-color="#4b5563"
        active-text-color="#111827"
      >
        <el-menu-item index="/">首页推荐</el-menu-item>
        <el-menu-item index="/all">全部景点</el-menu-item>
        <el-menu-item index="/favorites">我的收藏</el-menu-item>
        <el-menu-item index="/routes">经典路线</el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧用户信息与登出 -->
    <div class="navbar-right">
      <span class="nickname" v-if="nickname">
        欢迎，{{ nickname }}
      </span>
      <el-button type="text" @click="handleLogout">
        退出登录
      </el-button>
    </div>
  </header>
</template>

<script>
export default {
  name: 'Navbar',

  data () {
    return {
      nickname: window.localStorage.getItem('nickname') || ''
    }
  },

  computed: {
    /**
     * 当前激活的菜单路径。
     *
     * 使用 $route.path 作为 activeKey，可以在不同页面之间保持导航高亮状态。
     */
    activePath () {
      const path = this.$route.path
      // 将 /spot/:id 这类详情页统一映射为首页或其它菜单（这里先映射为首页）
      if (path.startsWith('/spot/')) {
        return '/'
      }
      return path
    }
  },

  methods: {
    /**
     * 退出登录：清空本地 Token 与用户信息，并跳转到登录页。
     */
    handleLogout () {
      window.localStorage.removeItem('token')
      window.localStorage.removeItem('userId')
      window.localStorage.removeItem('username')
      window.localStorage.removeItem('nickname')
      this.$message.success('已退出登录')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.navbar {
  height: 64px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  box-sizing: border-box;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.logo-circle {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: linear-gradient(135deg, #22c55e, #0ea5e9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-weight: 700;
  font-size: 16px;
  margin-right: 12px;
}

.logo-text {
  letter-spacing: 1px;
}

.title-wrapper {
  display: flex;
  flex-direction: column;
}

.app-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.app-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.navbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border-bottom: none;
}

.navbar-right {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.nickname {
  margin-right: 8px;
  color: #4b5563;
}

@media (max-width: 768px) {
  .navbar {
    padding: 0 16px;
  }

  .app-title {
    font-size: 16px;
  }

  .app-subtitle {
    display: none;
  }

  .navbar-center {
    display: none; /* 小屏可根据需要隐藏菜单，避免拥挤 */
  }
}
</style>

