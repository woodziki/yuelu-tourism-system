<template>
  <div class="home-page">
    <Navbar />

    <main class="home-main">
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">🎯 猜你喜欢 (专属推荐)</h2>
          <p class="section-desc">
            根据你的浏览、收藏与评分记录，为你生成兴趣画像和可解释推荐。
          </p>
        </div>

        <el-card class="profile-card" shadow="never">
          <div class="profile-left">
            <div class="profile-title-row">
              <h3 class="profile-title">我的兴趣画像</h3>
              <el-tag v-if="profileTopTags.length" type="success" size="mini">实时计算</el-tag>
            </div>
            <p class="profile-summary">{{ profile.summary || '暂无足够行为，先为你展示热门推荐' }}</p>
            <div class="profile-stats">
              <div class="stat-item">
                <span class="stat-value">{{ profile.viewCount || 0 }}</span>
                <span class="stat-label">浏览</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ profile.favoriteCount || 0 }}</span>
                <span class="stat-label">收藏</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ profile.commentCount || 0 }}</span>
                <span class="stat-label">评价</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ profile.averageRating || '--' }}</span>
                <span class="stat-label">均分</span>
              </div>
            </div>
            <div class="profile-tags">
              <el-tag
                v-for="tag in profileTopTags"
                :key="tag"
                size="small"
                type="success"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!profileTopTags.length" class="tag-placeholder">暂无兴趣标签</span>
            </div>
          </div>
          <div class="profile-chart" ref="profileChart"></div>
        </el-card>

        <div class="recommend-grid">
          <div v-if="!recommendList.length" class="recommend-empty">
            <el-empty description="当前暂无推荐数据，请先多多收藏和点评喜欢的景点～" />
          </div>

          <el-card
            v-for="item in recommendList"
            :key="item.spot.id"
            shadow="hover"
            class="spot-card"
            @click.native="goToDetail(item.spot.id)"
          >
            <div class="spot-cover">
              <img :src="item.spot.imageUrl || defaultCoverUrl" alt="景点封面">
            </div>

            <div class="spot-body">
              <div class="spot-title-row">
                <h3 class="spot-name" :title="item.spot.name">
                  {{ item.spot.name }}
                </h3>
                <div class="spot-score" v-if="item.spot.score != null">
                  <span class="score-number">{{ item.spot.score.toFixed(1) }}</span>
                  <span class="score-unit">分</span>
                </div>
              </div>

              <p class="spot-intro" :title="item.spot.intro">
                {{ item.spot.intro || '暂无简介' }}
              </p>

              <div class="reason-box">
                <i class="el-icon-magic-stick"></i>
                <span>{{ item.reason }}</span>
              </div>

              <div class="spot-footer">
                <div class="tag-list">
                  <el-tag
                    v-for="tag in parseTags(item.spot.tags)"
                    :key="tag"
                    size="mini"
                    type="success"
                    effect="plain"
                  >
                    {{ tag }}
                  </el-tag>
                  <span v-if="!parseTags(item.spot.tags).length" class="tag-placeholder">
                    无标签
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request'
import Navbar from '@/components/Navbar.vue'

export default {
  name: 'Home',

  components: {
    Navbar
  },

  data () {
    return {
      recommendList: [],
      profile: {},
      profileChart: null,
      defaultCoverUrl: 'https://api.dujin.org/bing/1920.php'
    }
  },

  computed: {
    profileTopTags () {
      return Array.isArray(this.profile.topTags) ? this.profile.topTags : []
    }
  },

  mounted () {
    this.fetchRecommendList()
    window.addEventListener('resize', this.resizeProfileChart)
  },

  beforeDestroy () {
    window.removeEventListener('resize', this.resizeProfileChart)
    if (this.profileChart) {
      this.profileChart.dispose()
    }
  },

  methods: {
    fetchRecommendList () {
      request({
        url: '/spot/recommendPage',
        method: 'get'
      })
        .then(res => {
          this.profile = (res && res.profile) || {}
          this.recommendList = Array.isArray(res && res.recommendations) ? res.recommendations : []
          this.$nextTick(this.renderProfileChart)
        })
        .catch(err => {
          console.error('获取推荐列表失败：', err)
        })
    },

    renderProfileChart () {
      if (!this.$refs.profileChart) return
      if (!this.profileChart) {
        this.profileChart = echarts.init(this.$refs.profileChart)
      }
      const weights = Array.isArray(this.profile.tagWeights) ? this.profile.tagWeights : []
      const data = weights.map(item => ({
        name: item.tag,
        value: item.weight
      }))
      this.profileChart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          bottom: 0,
          left: 'center',
          itemWidth: 10,
          itemHeight: 10,
          textStyle: {
            fontSize: 11
          }
        },
        series: [
          {
            name: '兴趣权重',
            type: 'pie',
            radius: ['42%', '68%'],
            center: ['50%', '42%'],
            avoidLabelOverlap: true,
            label: {
              formatter: '{b}'
            },
            data: data.length ? data : [{ name: '暂无行为', value: 1 }]
          }
        ]
      })
    },

    resizeProfileChart () {
      if (this.profileChart) {
        this.profileChart.resize()
      }
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
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6;
}

.home-main {
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

.profile-card {
  margin-bottom: 22px;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.profile-card ::v-deep .el-card__body {
  display: flex;
  gap: 20px;
  min-height: 220px;
  box-sizing: border-box;
}

.profile-left {
  flex: 1;
  min-width: 0;
}

.profile-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-title {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.profile-summary {
  margin: 10px 0 14px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(70px, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.stat-item {
  padding: 12px 10px;
  border-radius: 12px;
  background: #f9fafb;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #16a34a;
}

.stat-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-chart {
  width: 360px;
  min-height: 220px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.recommend-empty {
  grid-column: 1 / -1;
}

.spot-card {
  height: 100%;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
  cursor: pointer;
}

.spot-card ::v-deep .el-card__body {
  height: 100%;
  padding: 0;
  display: flex;
  flex-direction: column;
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
  flex: 1;
  padding: 14px 14px 12px;
  display: flex;
  flex-direction: column;
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

.reason-box {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  height: 40px;
  margin-bottom: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  background: #ecfdf5;
  color: #15803d;
  font-size: 12px;
  line-height: 1.5;
}

.spot-footer {
  margin-top: auto;
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

@media (max-width: 1100px) {
  .recommend-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .recommend-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .home-main {
    padding: 16px;
  }

  .section {
    max-width: 100%;
  }

  .recommend-grid {
    grid-template-columns: 1fr;
  }

  .profile-card ::v-deep .el-card__body {
    flex-direction: column;
  }

  .profile-stats {
    grid-template-columns: repeat(2, minmax(100px, 1fr));
  }

  .profile-chart {
    width: 100%;
  }
}
</style>
