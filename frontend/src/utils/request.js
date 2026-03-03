// src/utils/request.js

import axios from 'axios'
import { Message } from 'element-ui'
import router from '../router'

/**
 * 创建 axios 实例。
 *
 * <p>说明：</p>
 * <ul>
 *   <li>baseURL 统一设置为 /api，开发环境下通过 vue.config.js 代理到后端；</li>
 *   <li>timeout 设置为 5000ms，避免请求长时间挂起；</li>
 *   <li>后续所有业务接口统一基于该实例，方便集中处理 Token 与错误提示。</li>
 * </ul>
 */
const service = axios.create({
  baseURL: '/api', // 通过 vue.config.js 代理到 http://localhost:8080
  timeout: 5000 // 超时时间（毫秒）
})

/**
 * 请求拦截器：
 *
 * <p>在每次请求发送前自动执行，用于：</p>
 * <ul>
 *   <li>从 localStorage 读取登录成功后保存的 Token；</li>
 *   <li>如果存在 Token，则拼装成 "Bearer {token}" 格式，放入 Authorization 请求头中；</li>
 *   <li>避免每个接口手动设置 Token，保证鉴权逻辑统一。</li>
 * </ul>
 */
service.interceptors.request.use(
  config => {
    const token = window.localStorage.getItem('token')
    if (token) {
      // 注意中间的空格：Bearer {token}
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    // 请求还未发送就出现错误，一般是配置错误或前端代码异常
    console.error('请求拦截器错误：', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器：
 *
 * <p>后端统一返回 Result&lt;T&gt; 结构：{ code, message, data }</p>
 * <ul>
 *   <li>code === 200：业务成功，直接返回 data 给业务层使用；</li>
 *   <li>code === 401：未登录或 Token 失效，清除本地 token，弹错误提示，并跳转到 /login；</li>
 *   <li>其他业务错误：统一使用 Element UI 的 Message 弹出错误信息，并中断 Promise 链。</li>
 * </ul>
 */
service.interceptors.response.use(
  response => {
    const res = response.data // Result<T> 对象

    if (res.code === 200) {
      // 业务成功：直接返回 data，组件不必关心外层 Result 结构
      return res.data
    }

    // 统一处理 401：未登录或 Token 过期
    if (res.code === 401) {
      // 清除本地缓存的 Token
      window.localStorage.removeItem('token')

      // 弹出后端返回的错误提示（例如“请先登录”）
      Message.error(res.message || '未登录或登录已过期')

      // 强制跳转到登录页，避免停留在无权限页面
      if (router.currentRoute.path !== '/login') {
        router.push('/login')
      }

      // 中断 Promise 链，后续 then 不再执行
      return Promise.reject(new Error(res.message || '未登录或登录已过期'))
    }

    // 其他业务错误（code != 200 且 != 401）
    Message.error(res.message || '请求失败')

    // 中断 Promise 链，交由调用方的 catch 处理
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    // 这里是 HTTP 层面的错误（网络错误、超时、后端 500 等），没有进入正常的业务逻辑
    console.error('响应拦截器错误：', error)

    if (error.response && error.response.data) {
      const status = error.response.status
      const res = error.response.data
      const msg = res.message || `请求失败，HTTP 状态码：${status}`
      Message.error(msg)

      // 后端如果在异常时返回了 401，同样执行登出与跳转逻辑
      if (status === 401 || res.code === 401) {
        window.localStorage.removeItem('token')
        if (router.currentRoute.path !== '/login') {
          router.push('/login')
        }
      }
    } else {
      // 没有任何响应，一般是网络断开或跨域配置问题
      Message.error('网络异常，请检查后端服务是否已启动或网络连接是否正常')
    }

    return Promise.reject(error)
  }
)

export default service