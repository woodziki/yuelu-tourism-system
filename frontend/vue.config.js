// vue.config.js

/**
 * Vue CLI 的配置文件。
 *
 * <p>目的：在开发环境中解决前端本地端口（如 8081）与后端 Spring Boot（8080）之间的跨域问题。</p>
 *
 * <p>实现方式：通过 devServer.proxy 将所有以 /api 开头的请求代理到后端，
 * 并通过 pathRewrite 去掉 /api 前缀，这样前端代码始终只需请求 /api/xxx。</p>
 */
module.exports = {
  lintOnSave: false,
  devServer: {
    proxy: {
      // 只要是以 /api 开头的请求，都会被代理到后端
      '/api': {
        target: 'http://localhost:8080', // 后端服务地址
        changeOrigin: true, // 是否修改请求源（避免某些后端对 Host 有限制）
        pathRewrite: {
          '^/api': '' // 去掉请求路径中的 /api 前缀，例如 /api/user/login -> /user/login
        }
      }
    }
  }
}