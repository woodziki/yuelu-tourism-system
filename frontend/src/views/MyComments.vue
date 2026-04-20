<template>
  <div class="my-comments-page">
    <Navbar />

    <main class="comments-main">
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">我的评论</h2>
          <p class="section-desc">查看你发布过的景点评价，点击景点名称可回到详情页。</p>
        </div>

        <el-card class="comment-list-card" shadow="never">
          <div v-if="loading" class="loading-wrapper">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="!comments.length" class="empty-wrapper">
            <el-empty description="你还没有发布过评论，去景点详情页写下第一条评价吧～" />
          </div>
          <div v-else>
            <el-card
              v-for="item in comments"
              :key="item.id"
              class="comment-card"
              shadow="never"
            >
              <div class="comment-header">
                <div>
                  <span class="spot-name" @click="goToDetail(item.spotId)">
                    {{ item.spotName || '未知景点' }}
                  </span>
                  <span class="comment-time">{{ item.createTime || item.time || '时间未知' }}</span>
                </div>
                <el-rate :value="item.star" disabled text-color="#f59e0b" />
              </div>
              <p class="comment-content">{{ item.content || '（未填写文字评价）' }}</p>
            </el-card>

            <div class="pagination-wrapper" v-if="commentPage.total > commentPage.size">
              <el-pagination
                background
                layout="prev, pager, next, total"
                :current-page="commentPage.current"
                :page-size="commentPage.size"
                :total="commentPage.total"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </el-card>
      </section>
    </main>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import request from '@/utils/request'

export default {
  name: 'MyComments',

  components: {
    Navbar
  },

  data () {
    return {
      comments: [],
      loading: false,
      commentPage: {
        current: 1,
        size: 10,
        total: 0
      }
    }
  },

  mounted () {
    this.fetchComments()
  },

  methods: {
    fetchComments () {
      this.loading = true
      request({
        url: '/comment/my',
        method: 'get',
        params: {
          current: this.commentPage.current,
          size: this.commentPage.size
        }
      })
        .then(res => {
          this.comments = Array.isArray(res && res.records) ? res.records : []
          this.commentPage.total = Number((res && res.total) || 0)
          this.commentPage.current = Number((res && res.current) || 1)
          this.commentPage.size = Number((res && res.size) || this.commentPage.size)
        })
        .catch(err => {
          console.error('加载我的评论失败：', err)
          this.comments = []
          this.commentPage.total = 0
        })
        .finally(() => {
          this.loading = false
        })
    },

    handlePageChange (page) {
      this.commentPage.current = page
      this.fetchComments()
    },

    goToDetail (spotId) {
      if (!spotId) return
      this.$router.push('/spot/' + spotId)
    }
  }
}
</script>

<style scoped>
.my-comments-page {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.comments-main {
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

.comment-list-card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
}

.comment-card {
  margin-bottom: 12px;
  border-radius: 12px;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.spot-name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  cursor: pointer;
}

.spot-name:hover {
  color: #16a34a;
}

.comment-time {
  margin-left: 10px;
  font-size: 12px;
  color: #9ca3af;
}

.comment-content {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
}

.loading-wrapper,
.empty-wrapper {
  padding: 40px 0;
}

.pagination-wrapper {
  margin-top: 18px;
  text-align: right;
}

@media (max-width: 768px) {
  .comments-main {
    padding: 16px;
  }

  .comment-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
