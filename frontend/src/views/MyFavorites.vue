<template>
  <div class="favorites-page">
    <Navbar />

    <main class="favorites-main">
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">📂 我的收藏</h2>
          <p class="section-desc">
            这里集中展示你在岳麓山中标记为“特别喜欢”的景点，便于再次查看与规划行程。
          </p>
        </div>

        <el-row :gutter="24">
          <!-- 空状态 -->
          <el-col v-if="!favoriteList.length && !loading" :span="24">
            <div class="empty-wrapper">
              <el-empty description="当前还没有收藏任何景点～">
                <el-button type="primary" @click="$router.push('/')">
                  去看看推荐
                </el-button>
              </el-empty>
            </div>
          </el-col>

          <!-- 加载中状态 -->
          <el-col v-if="loading" :span="24">
            <div class="loading-wrapper">
              <el-skeleton :rows="4" animated />
            </div>
          </el-col>

          <!-- 收藏景点卡片 -->
          <el-col
            v-for="item in favoriteList"
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
              <div class="spot-cover">
                <img
                  :src="item.imageUrl || defaultCoverUrl"
                  alt="景点封面"
                >
              </div>

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
                    <span
                      v-if="!parseTags(item.tags).length"
                      class="tag-placeholder"
                    >
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
import Navbar from '@/components/Navbar.vue'
import request from '@/utils/request'

export default {
  name: 'MyFavorites',

  components: {
    Navbar
  },

  data () {
    return {
      favoriteList: [],
      defaultCoverUrl: 'https://api.dujin.org/bing/1920.php',
      loading: false
    }
  },

  mounted () {
    this.fetchFavorites()
  },

  methods: {
    /**
     * 加载当前用户的收藏景点列表。
     *
     * 接口：GET /favorite/list
     */
    fetchFavorites () {
      this.loading = true
      request({
        url: '/favorite/list',
        method: 'get'
      })
        .then(res => {
          this.favoriteList = Array.isArray(res) ? res : []
        })
        .catch(err => {
          console.error('加载收藏列表失败：', err)
          this.favoriteList = []
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

    goToDetail (id) {
      if (!id) return
      this.$router.push('/spot/' + id)
    }
  }
}
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6;
}

.favorites-main {
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

.spot-card {
  margin-bottom: 24px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
  cursor: pointer;
}

.spot-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 12px 24px rgba(15, 23, 42, 0.1),
    0 0 0 1px rgba(148, 163, 184, 0.5);
}

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

.spot-body {
  padding: 14px 14px 12px;
}

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
  color: #f97316;
  margin-right: 2px;
}

.score-unit {
  font-size: 12px;
  color: #f59e0b;
}

.spot-intro {
  margin: 0 0 10px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

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

.empty-wrapper,
.loading-wrapper {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .favorites-main {
    padding: 16px;
  }
}
</style>

