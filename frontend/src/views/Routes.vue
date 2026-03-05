<template>
  <div class="routes-page">
    <Navbar />

    <main class="routes-main">
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">🗺️ 经典游览路线</h2>
          <p class="section-desc">
            为你精心规划的岳麓山游玩路线，从山门到山顶，一路串联必去景点。
          </p>
        </div>

        <el-row :gutter="24">
          <el-col v-if="loading" :span="24">
            <div class="loading-wrapper">
              <el-skeleton :rows="4" animated />
            </div>
          </el-col>

          <el-col v-if="!loading && !routes.length" :span="24">
            <div class="empty-wrapper">
              <el-empty description="暂时没有配置经典路线～" />
            </div>
          </el-col>

          <el-col
            v-for="route in routes"
            :key="route.id"
            :span="24"
          >
            <el-card class="route-card" shadow="hover">
              <div class="route-header">
                <div class="route-title-block">
                  <h3 class="route-name">{{ route.name }}</h3>
                  <p class="route-intro">
                    {{ route.intro || '暂无简介' }}
                  </p>
                  <div class="route-tags" v-if="route.tags">
                    <el-tag
                      v-for="tag in parseTags(route.tags)"
                      :key="tag"
                      size="mini"
                      type="info"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </div>

              <div class="route-banner" v-if="route.mapImg">
                <img :src="route.mapImg" alt="路线地图">
              </div>

              <div class="route-body">
                <el-timeline>
                  <el-timeline-item
                    v-for="(spot, index) in route.spots"
                    :key="spot.id || index"
                    :timestamp="'第 ' + (index + 1) + ' 站'"
                    placement="top"
                    color="#0ea5e9"
                  >
                    <div class="spot-item">
                      <div class="spot-thumb" @click="goToSpot(spot.id)">
                        <img
                          :src="spot.imageUrl || defaultCoverUrl"
                          alt="景点封面"
                        >
                      </div>
                      <div class="spot-info">
                        <div
                          class="spot-name"
                          @click="goToSpot(spot.id)"
                        >
                          {{ spot.name }}
                        </div>
                        <div class="spot-meta">
                          <span v-if="spot.duration" class="meta-badge">
                            时长：{{ spot.duration }}
                          </span>
                          <span v-if="spot.price != null" class="meta-badge">
                            费用：￥{{ spot.price }}
                          </span>
                          <span v-if="spot.score != null" class="meta-score">
                            {{ spot.score.toFixed(1) }} 分
                          </span>
                        </div>
                        <p class="spot-intro">
                          {{ spot.intro || '暂无简介' }}
                        </p>
                      </div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </section>
    </main>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import request from '@/utils/request'

export default {
  name: 'Routes',

  components: {
    Navbar
  },

  data () {
    return {
      routes: [],
      defaultCoverUrl: 'https://api.dujin.org/bing/1920.php',
      loading: false
    }
  },

  mounted () {
    this.fetchRoutes()
  },

  methods: {
    /**
     * 加载经典游览路线列表。
     *
     * 接口：GET /route/list，返回 RouteVO 列表（包含 spots）。
     */
    fetchRoutes () {
      this.loading = true
      request({
        url: '/route/list',
        method: 'get'
      })
        .then(res => {
          this.routes = Array.isArray(res) ? res : []
        })
        .catch(err => {
          console.error('加载经典路线失败：', err)
          this.routes = []
        })
        .finally(() => {
          this.loading = false
        })
    },

    parseTags (tagsStr) {
      if (!tagsStr) return []
      return tagsStr
        .split(',')
        .map(t => t.trim())
        .filter(Boolean)
    },

    goToSpot (id) {
      if (!id) return
      this.$router.push('/spot/' + id)
    }
  }
}
</script>

<style scoped>
.routes-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6;
}

.routes-main {
  flex: 1;
  padding: 24px 32px 32px;
  box-sizing: border-box;
}

.section {
  max-width: 1200px;
  margin: 0 auto;
}

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

.route-card {
  margin-bottom: 24px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
}

.route-header {
  margin-bottom: 12px;
}

.route-name {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.route-intro {
  margin: 0 0 6px;
  font-size: 13px;
  color: #4b5563;
}

.route-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 🌟 修改点：横幅大图样式 */
.route-banner {
  width: 100%;
  height: 240px; 
  margin: 16px 0 24px 0; 
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.route-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover; 
  transition: transform 0.5s ease;
}

.route-banner:hover img {
  transform: scale(1.02); 
}

.route-body {
  margin-top: 8px;
}

.spot-item {
  display: flex;
  gap: 10px;
}

.spot-thumb {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #e5e7eb;
  cursor: pointer;
}

.spot-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.spot-thumb:hover img {
  transform: scale(1.05);
}

.spot-info {
  flex: 1;
}

.spot-name {
  font-size: 14px;
  font-weight: 600;
  color: #0f766e;
  cursor: pointer;
}

.spot-name:hover {
  text-decoration: underline;
}

.spot-meta {
  margin: 2px 0 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meta-badge {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 999px;
  background-color: #ecfeff;
  color: #0e7490;
}

.meta-score {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 999px;
  background-color: #fffbeb;
  color: #f97316;
}

.spot-intro {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.loading-wrapper,
.empty-wrapper {
  padding: 40px 0;
}

/* 移动端适配优化 */
@media (max-width: 768px) {
  .routes-main {
    padding: 16px;
  }

  .route-banner {
  width: 100%;
  aspect-ratio: 1 / 1; /* 强制 1:1 正方形比例 */
  margin: 16px 0 24px 0; 
  border-radius: 12px;
  overflow: hidden;
}
}
</style>