<template>
  <div class="all-spots-page">
    <Navbar />

    <main class="all-main">
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">⛰️ 探索岳麓 (全部景点)</h2>
          <p class="section-desc">
            浏览所有景点，发现更多惊喜，为你的岳麓山之旅找到更多灵感。
          </p>
        </div>

        <el-card class="ranking-card" shadow="never">
          <div class="ranking-header">
            <div>
              <h3 class="ranking-title">景点排行榜</h3>
              <p class="ranking-desc">从热度、评分和收藏三个角度发现值得一看的景点，热度值来自景点基础展示数据。</p>
            </div>
          </div>
          <el-tabs v-model="activeRankingType" @tab-click="fetchRankings">
            <el-tab-pane label="热度榜" name="hot" />
            <el-tab-pane label="高分榜" name="score" />
            <el-tab-pane label="收藏榜" name="favorite" />
          </el-tabs>
          <div v-if="rankingLoading" class="ranking-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else class="ranking-list">
            <div
              v-for="item in rankings"
              :key="item.spot.id"
              class="ranking-item"
              @click="goToDetail(item.spot.id)"
            >
              <span class="ranking-index">{{ item.rank }}</span>
              <span class="ranking-name">{{ item.spot.name }}</span>
              <span class="ranking-label">{{ formatRankingLabel(item.label) }}</span>
            </div>
          </div>
        </el-card>

        <div class="filter-bar">
          <div class="search-area">
            <el-input
              v-model="searchName"
              placeholder="输入景点名称搜索（回车或点击搜索）"
              clearable
              prefix-icon="el-icon-search"
              @keyup.enter.native="onSearch"
            >
              <el-button slot="append" type="primary" @click="onSearch">
                搜索
              </el-button>
            </el-input>
          </div>

          <div class="tag-filter">
            <span class="filter-label">标签筛选：</span>
            <el-radio-group
              v-model="activeTag"
              size="small"
              @change="onTagChange"
            >
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="历史人文">历史人文</el-radio-button>
              <el-radio-button label="自然风光">自然风光</el-radio-button>
              <el-radio-button label="宗教祈福">宗教祈福</el-radio-button>
              <el-radio-button label="现代游乐">现代游乐</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <el-row :gutter="24">
          <el-col v-if="!spotList.length && !loading" :span="24">
            <div class="empty-wrapper">
              <el-empty description="暂时没有可展示的景点数据～" />
            </div>
          </el-col>

          <el-col v-if="loading" :span="24">
            <div class="loading-wrapper">
              <el-skeleton :rows="4" animated />
            </div>
          </el-col>

          <el-col
            v-for="item in spotList"
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
                <img :src="item.imageUrl || defaultCoverUrl" alt="景点封面">
              </div>

              <div class="spot-body">
                <div class="spot-title-row">
                  <h3 class="spot-name" :title="item.name">
                    {{ item.name }}
                  </h3>
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
                  <div class="spot-score" v-if="item.score != null">
                    <span class="score-number">{{ item.score.toFixed(1) }}</span>
                    <span class="score-unit">分</span>
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
  name: 'AllSpots',

  components: {
    Navbar
  },

  data () {
    return {
      spotList: [],
      rankings: [],
      activeRankingType: 'hot',
      rankingLoading: false,
      defaultCoverUrl: 'https://api.dujin.org/bing/1920.php',
      loading: false,
      searchName: '',
      activeTag: ''
    }
  },

  mounted () {
    this.fetchSpots()
    this.fetchRankings()
  },

  methods: {
    fetchRankings () {
      this.rankingLoading = true
      request({
        url: '/spot/rankings',
        method: 'get',
        params: {
          type: this.activeRankingType,
          limit: 10
        }
      })
        .then(res => {
          this.rankings = Array.isArray(res) ? res : []
        })
        .catch(err => {
          console.error('加载景点排行榜失败：', err)
          this.rankings = []
        })
        .finally(() => {
          this.rankingLoading = false
        })
    },

    fetchSpots () {
      this.loading = true
      request({
        url: '/spot/list',
        method: 'get',
        params: {
          current: 1,
          size: 100,
          name: this.searchName || undefined,
          tags: this.activeTag || undefined
        }
      })
        .then(res => {
          this.spotList = (res && Array.isArray(res.records)) ? res.records : []
        })
        .catch(err => {
          console.error('加载景点列表失败：', err)
          this.spotList = []
        })
        .finally(() => {
          this.loading = false
        })
    },

    onSearch () {
      this.fetchSpots()
    },

    onTagChange () {
      this.fetchSpots()
    },

    parseTags (tagsStr) {
      if (!tagsStr) return []
      return tagsStr
        .split(',')
        .map(t => t.trim())
        .filter(Boolean)
    },

    formatRankingLabel (label) {
      if (!label) return ''
      return label.replace('次浏览', '热度值')
    },

    goToDetail (id) {
      if (!id) return
      this.$router.push('/spot/' + id)
    }
  }
}
</script>

<style scoped>
.all-spots-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6;
}

.all-main {
  flex: 1;
  padding: 24px 32px 32px;
  box-sizing: border-box;
}

.section {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  margin-bottom: 12px;
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

.ranking-card {
  margin: 12px 0 18px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
}

.ranking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-title {
  margin: 0 0 4px;
  font-size: 18px;
  color: #111827;
}

.ranking-desc {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.ranking-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ranking-item:hover {
  background: #ecfdf5;
  transform: translateY(-2px);
}

.ranking-index {
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: #16a34a;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.ranking-name {
  flex: 1;
  min-width: 0;
  color: #111827;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ranking-label {
  font-size: 12px;
  color: #f97316;
}

.ranking-loading {
  padding: 8px 0;
}

.filter-bar {
  margin: 8px 0 20px;
  padding: 10px 14px;
  border-radius: 10px;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 20px;
}

.search-area {
  flex: 1 1 260px;
}

.tag-filter {
  display: flex;
  align-items: center;
  flex: 1 1 260px;
  justify-content: flex-end;
  min-width: 260px;
}

.filter-label {
  margin-right: 8px;
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
  .all-main {
    padding: 16px;
  }

  .filter-bar {
    padding: 10px;
  }

  .tag-filter {
    justify-content: flex-start;
  }

  .ranking-list {
    grid-template-columns: 1fr;
  }
}
</style>
