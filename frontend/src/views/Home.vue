<template>
  <div class="home-page">
    <Navbar />

    <!-- 主体内容 -->
    <main class="home-main">
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">🎯 猜你喜欢 (专属推荐)</h2>
          <p class="section-desc">
            根据你的收藏与评分记录，为你智能推荐更符合你口味的岳麓山景点。
          </p>
        </div>

        <el-row :gutter="24">
          <!-- 无数据时的空状态 -->
          <el-col v-if="!recommendList.length" :span="24">
            <el-empty
              description="当前暂无推荐数据，请先多多收藏和点评喜欢的景点～"
            />
          </el-col>

          <!-- 推荐景点卡片 -->
          <el-col
            v-for="item in recommendList"
            :key="item.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-card
              shadow="hover"
              class="spot-card"
              @click.native="goToDetail(item.id)"
            >
              <!-- 顶部图片占位 -->
              <div class="spot-cover">
                <img
                  :src="item.imageUrl || defaultCoverUrl"
                  alt="景点封面"
                >
              </div>

              <!-- 卡片正文 -->
              <div class="spot-body">
                <div class="spot-title-row">
                  <h3 class="spot-name" :title="item.name">
                    {{ item.name }}
                  </h3>
                  <div class="spot-score" v-if="item.score != null">
                    <span class="score-number">{{ item.score.toFixed(1) }}</span>
                    <span class="score-unit">分</span>
                  </div>
                </div>

                <p class="spot-intro" :title="item.intro">
                  {{ item.intro || '暂无简介' }}
                </p>

                <div class="spot-footer">
                  <div class="tag-list">
                    <el-tag
                      v-for="tag in parseTags(item.tags)"
                      :key="tag"
                      size="mini"
                      type="success"
                      effect="plain"
                    >
                      {{ tag }}
                    </el-tag>
                    <span v-if="!parseTags(item.tags).length" class="tag-placeholder">
                      无标签
                    </span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </section>
    </main>
  </div>
</template>

<script>
import request from '@/utils/request'
import Navbar from '@/components/Navbar.vue'

export default {
  name: 'Home',

  components: {
    Navbar
  },

  data () {
    return {
      // 推荐景点列表（从 /spot/recommend 获取）
      recommendList: [],
      // 默认封面图（使用高质量自然风景图）
      defaultCoverUrl: 'https://api.dujin.org/bing/1920.php'
    }
  },

  mounted () {
    // 组件挂载后立刻加载推荐列表
    this.fetchRecommendList()
  },

  methods: {
    /**
     * 调用后端协同过滤推荐接口，获取「猜你喜欢」列表。
     */
    fetchRecommendList () {
      request({
        url: '/spot/recommend',
        method: 'get'
      })
        .then(res => {
          // res 即为 Result<T>.data（List<Spot>），直接赋值给列表
          this.recommendList = Array.isArray(res) ? res : []
        })
        .catch(err => {
          // 错误信息已在响应拦截器里统一弹出，这里只做调试打印
          console.error('获取推荐列表失败：', err)
        })
    },

    /**
     * 解析后端返回的标签字符串（"人文,古迹" -> ["人文","古迹"]）。
     */
    parseTags (tagsStr) {
      if (!tagsStr) {
        return []
      }
      return tagsStr
        .split(',')
        .map(t => t.trim())
        .filter(Boolean)
    },

    /**
     * 跳转到景点详情页。
     *
     * @param {number|string} id 景点 ID
     */
    goToDetail (id) {
      if (!id) return
      this.$router.push('/spot/' + id)
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6; /* 极浅灰色背景，突出白色卡片 */
}

/* 顶部导航栏 */
.home-header {
  height: 64px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05); /* 轻微底部阴影 */
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: center;
}

/* 左侧圆形 Logo，占位用字母 YL 代表岳麓 */
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

.header-right {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.nickname {
  margin-right: 8px;
  color: #4b5563;
}

/* 主体内容区域 */
.home-main {
  flex: 1;
  padding: 24px 32px 32px;
  box-sizing: border-box;
}

/* 核心模块容器 */
.section {
  max-width: 1200px;
  margin: 0 auto;
}

/* 模块标题与描述 */
.section-header {
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.section-desc {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

/* 景点卡片整体样式 */
.spot-card {
  margin-bottom: 24px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
  cursor: pointer; /* 鼠标移上去显示小手，提示可点击 */
}

/* 卡片悬浮时放大并加深阴影，营造轻盈的交互感 */
.spot-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 12px 24px rgba(15, 23, 42, 0.1),
    0 0 0 1px rgba(148, 163, 184, 0.5);
}

/* 顶部封面图区域 */
.spot-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background-color: #e5e7eb;
}

.spot-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.5s ease;
}

.spot-card:hover .spot-cover img {
  transform: scale(1.05);
}

/* 卡片正文内容 */
.spot-body {
  padding: 14px 14px 12px;
}

/* 标题与评分一行 */
.spot-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.spot-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  max-width: 70%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 评分展示，橙色高亮 */
.spot-score {
  display: inline-flex;
  align-items: baseline;
  background-color: #fffbeb;
  border-radius: 999px;
  padding: 2px 8px;
}

.score-number {
  font-size: 14px;
  font-weight: 600;
  color: #f97316; /* 橙色 */
  margin-right: 2px;
}

.score-unit {
  font-size: 12px;
  color: #f59e0b;
}

/* 简介文案：灰色小字，超出两行省略号 */
.spot-intro {
  margin: 0 0 10px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2; /* 最多显示两行 */
  overflow: hidden;
}

/* 底部标签区域 */
.spot-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-placeholder {
  font-size: 12px;
  color: #9ca3af;
}

/* 小屏适配：减小左右边距 */
@media (max-width: 768px) {
  .home-main {
    padding: 16px;
  }

  .home-header {
    padding: 0 16px;
  }

  .section {
    max-width: 100%;
  }
}
</style>