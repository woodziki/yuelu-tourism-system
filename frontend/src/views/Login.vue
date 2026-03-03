<template>
  <div class="login-page">
    <!-- 中央毛玻璃面板 -->
    <div class="glass-card">
      <h2 class="app-title">岳麓山景点推荐系统</h2>
      <p class="app-subtitle">基于协同过滤的个性化旅游推荐平台</p>

      <el-tabs v-model="activeTab" stretch class="login-tabs">
        <!-- 登录 Tab -->
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginForm"
            :model="loginForm"
            :rules="loginRules"
            label-width="80px"
            status-icon
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                clearable
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loginLoading"
                class="full-width-btn"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 注册 Tab -->
        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerForm"
            :model="registerForm"
            :rules="registerRules"
            label-width="80px"
            status-icon
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名（登录使用）"
                clearable
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                show-password
                placeholder="请设置登录密码（至少 6 位）"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入密码"
              />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="请输入昵称（展示用）"
                clearable
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="registerLoading"
                class="full-width-btn"
                @click="handleRegister"
              >
                注册并创建账号
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Login',

  data () {
    // 自定义校验：确认密码必须与密码一致
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请再次输入密码'))
      }
      if (value !== this.registerForm.password) {
        return callback(new Error('两次输入的密码不一致'))
      }
      return callback()
    }

    return {
      // 当前激活的 Tab：login / register
      activeTab: 'login',

      // 登录表单数据
      loginForm: {
        username: '',
        password: ''
      },
      // 注册表单数据
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        nickname: ''
      },

      // 登录按钮 loading 状态
      loginLoading: false,
      // 注册按钮 loading 状态
      registerLoading: false,

      // 登录表单校验规则
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
        ]
      },

      // 注册表单校验规则
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请设置密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ]
      }
    }
  },

  methods: {
    /**
     * 处理登录逻辑。
     *
     * <p>步骤：</p>
     * <ol>
     *   <li>先通过 Element UI 的表单进行前端校验；</li>
     *   <li>调用后端 /user/login 接口；</li>
     *   <li>成功后将 token / userId / username / nickname 存入 localStorage；</li>
     *   <li>给出成功提示，并跳转到首页 /。</li>
     * </ol>
     */
    handleLogin () {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return

        this.loginLoading = true
        request({
          url: '/user/login',
          method: 'post',
          data: this.loginForm
        })
          .then(res => {
            // 此处 res 即为 Result<T>.data（LoginVO），结构：token/userId/username/nickname
            const { token, userId, username, nickname } = res || {}

            // 将关键信息存入浏览器本地，后续路由与请求会用到
            window.localStorage.setItem('token', token || '')
            window.localStorage.setItem('userId', String(userId || ''))
            window.localStorage.setItem('username', username || '')
            window.localStorage.setItem('nickname', nickname || '')

            this.$message.success('登录成功，欢迎回来！')
            // 跳转到首页（推荐页）
            this.$router.push('/')
          })
          .catch(err => {
            // 这里的错误已在拦截器中弹出 message，这里仅做兜底提示
            console.error('登录失败：', err)
            if (!err || !err.message) {
              this.$message.error('登录失败，请稍后重试')
            }
          })
          .finally(() => {
            this.loginLoading = false
          })
      })
    },

    /**
     * 处理注册逻辑。
     *
     * <p>步骤：</p>
     * <ol>
     *   <li>前端表单校验（必填 + 密码长度 + 两次密码一致）；</li>
     *   <li>调用 /user/register 接口创建新账号；</li>
     *   <li>注册成功后提示，并自动切换回“登录” Tab，让用户立刻登录。</li>
     * </ol>
     */
    handleRegister () {
      this.$refs.registerForm.validate(valid => {
        if (!valid) return

        this.registerLoading = true
        request({
          url: '/user/register',
          method: 'post',
          data: {
            username: this.registerForm.username,
            password: this.registerForm.password,
            nickname: this.registerForm.nickname
          }
        })
          .then(() => {
            this.$message.success('注册成功，请使用新账号登录')
            // 切回登录 Tab
            this.activeTab = 'login'
            // 可选：重置登录表单中的用户名，方便用户直接输入密码
            this.loginForm.username = this.registerForm.username
          })
          .catch(err => {
            console.error('注册失败：', err)
            if (!err || !err.message) {
              this.$message.error('注册失败，请稍后重试')
            }
          })
          .finally(() => {
            this.registerLoading = false
          })
      })
    }
  }
}
</script>

<style scoped>
/* 整个登录页容器：全屏铺满 + 居中对齐 */
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}

/* 使用 ::before 伪元素铺设全屏背景图，支持高斯模糊与渐变遮罩 */
.login-page::before {
  content: '';
  position: fixed;
  inset: 0;
  background-image:
    linear-gradient(to bottom right, rgba(10, 24, 61, 0.55), rgba(8, 47, 73, 0.65)),
    url('https://api.dujin.org/bing/1920.php');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(4px);
  transform: scale(1.03);
  z-index: -2;
}

/* 半透明毛玻璃面板 */
.glass-card {
  width: 420px;
  max-width: 100%;
  padding: 32px 32px 24px;
  border-radius: 18px;
  box-sizing: border-box;

  /* 毛玻璃核心：半透明背景 + 背景虚化 + 细描边 + 阴影 */
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow:
    0 18px 45px rgba(15, 23, 42, 0.45),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);

  color: #f9fafb;
}

/* 标题与副标题 */
.app-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-align: center;
}

.app-subtitle {
  margin: 0 0 24px;
  font-size: 13px;
  color: rgba(226, 232, 240, 0.9);
  text-align: center;
}

/* Tabs 样式微调，使之更适合毛玻璃风格 */
.login-tabs ::v-deep .el-tabs__header {
  margin-bottom: 18px;
  border-bottom: none;
}

.login-tabs ::v-deep .el-tabs__nav-wrap::after {
  height: 0;
}

.login-tabs ::v-deep .el-tabs__item {
  color: rgba(226, 232, 240, 0.7);
  font-weight: 500;
}

.login-tabs ::v-deep .el-tabs__item.is-active {
  color: #ffffff;
}

.login-tabs ::v-deep .el-tabs__active-bar {
  height: 3px;
  border-radius: 999px;
  background-image: linear-gradient(90deg, #22c55e, #14b8a6);
}

/* 表单中的按钮全宽展示，符合登录场景的交互习惯 */
.full-width-btn {
  width: 100%;
}

/* 小屏兼容：在手机上适当减小内边距与宽度 */
@media (max-width: 480px) {
  .glass-card {
    padding: 24px 20px 18px;
    border-radius: 14px;
  }

  .app-title {
    font-size: 20px;
  }

  .app-subtitle {
    font-size: 12px;
  }
}
</style>