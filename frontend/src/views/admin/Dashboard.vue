<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2 class="page-title">数据看板</h2>
      <p class="page-desc">集中展示系统访问、用户行为、景点评价和推荐相关数据；真实浏览行为来自登录用户浏览记录，展示热度值来自景点表 view_count。</p>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col v-for="item in statCards" :key="item.label" :xs="12" :sm="8" :md="6" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <div class="card-title">景点标签分布</div>
          <div class="chart" ref="tagChart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <div class="card-title">评分区间分布</div>
          <div class="chart" ref="scoreChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="ranking-row">
      <el-col :xs="24" :md="8">
        <el-card class="list-card" shadow="never">
          <div class="card-title">热度榜 Top5</div>
          <div v-for="item in dashboard.hotRankings" :key="item.spot.id" class="ranking-item">
            <span class="rank">{{ item.rank }}</span>
            <span class="name">{{ item.spot.name }}</span>
            <span class="value">{{ item.label }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="list-card" shadow="never">
          <div class="card-title">高分榜 Top5</div>
          <div v-for="item in dashboard.scoreRankings" :key="item.spot.id" class="ranking-item">
            <span class="rank">{{ item.rank }}</span>
            <span class="name">{{ item.spot.name }}</span>
            <span class="value">{{ item.label }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="list-card" shadow="never">
          <div class="card-title">收藏榜 Top5</div>
          <div v-for="item in dashboard.favoriteRankings" :key="item.spot.id" class="ranking-item">
            <span class="rank">{{ item.rank }}</span>
            <span class="name">{{ item.spot.name }}</span>
            <span class="value">{{ item.label }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="comment-card" shadow="never">
      <div class="card-title">最新评论</div>
      <el-table :data="dashboard.latestComments" stripe style="width: 100%">
        <el-table-column prop="nickname" label="用户" width="120" />
        <el-table-column prop="spotName" label="景点" width="160" />
        <el-table-column prop="star" label="评分" width="80" />
        <el-table-column prop="content" label="评论内容" />
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request'

export default {
  name: 'AdminDashboard',

  data () {
    return {
      dashboard: {
        stats: {},
        tagStats: [],
        scoreRangeStats: [],
        hotRankings: [],
        scoreRankings: [],
        favoriteRankings: [],
        latestComments: []
      },
      tagChart: null,
      scoreChart: null
    }
  },

  computed: {
    statCards () {
      const stats = this.dashboard.stats || {}
      return [
        { label: '用户数', value: stats.userCount || 0 },
        { label: '景点数', value: stats.spotCount || 0 },
        { label: '评论数', value: stats.commentCount || 0 },
        { label: '收藏数', value: stats.favoriteCount || 0 },
        { label: '真实浏览行为', value: stats.viewRecordCount || 0 },
        { label: '展示热度值', value: stats.totalViewCount || 0 },
        { label: '平均评分', value: stats.averageScore || '--' }
      ]
    }
  },

  mounted () {
    this.fetchDashboard()
    window.addEventListener('resize', this.resizeCharts)
  },

  beforeDestroy () {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.tagChart) this.tagChart.dispose()
    if (this.scoreChart) this.scoreChart.dispose()
  },

  methods: {
    fetchDashboard () {
      request({
        url: '/admin/dashboard',
        method: 'get'
      })
        .then(res => {
          this.dashboard = res || this.dashboard
          this.$nextTick(this.renderCharts)
        })
        .catch(err => {
          console.error('加载后台数据看板失败：', err)
        })
    },

    renderCharts () {
      this.renderTagChart()
      this.renderScoreChart()
    },

    renderTagChart () {
      if (!this.$refs.tagChart) return
      if (!this.tagChart) {
        this.tagChart = echarts.init(this.$refs.tagChart)
      }
      const data = (this.dashboard.tagStats || []).map(item => ({
        name: item.tag,
        value: item.count
      }))
      this.tagChart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [
          {
            name: '标签数量',
            type: 'pie',
            radius: ['42%', '68%'],
            center: ['50%', '42%'],
            data
          }
        ]
      })
    },

    renderScoreChart () {
      if (!this.$refs.scoreChart) return
      if (!this.scoreChart) {
        this.scoreChart = echarts.init(this.$refs.scoreChart)
      }
      const rows = this.dashboard.scoreRangeStats || []
      this.scoreChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: rows.map(item => item.range)
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: '景点数',
            type: 'bar',
            data: rows.map(item => item.count),
            itemStyle: { color: '#16a34a' }
          }
        ]
      })
    },

    resizeCharts () {
      if (this.tagChart) this.tagChart.resize()
      if (this.scoreChart) this.scoreChart.resize()
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  color: #111827;
}

.page-desc {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.stats-row,
.ranking-row {
  margin-bottom: 16px;
}

.stat-card,
.chart-card,
.list-card,
.comment-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #16a34a;
}

.card-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.chart {
  height: 300px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.ranking-item:last-child {
  border-bottom: none;
}

.rank {
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

.name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.value {
  font-size: 12px;
  color: #f97316;
}
</style>
