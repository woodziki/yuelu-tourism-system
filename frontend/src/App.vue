<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style>
/* 清除浏览器默认边距，让背景图能 100% 铺满全屏 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
}
#app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>