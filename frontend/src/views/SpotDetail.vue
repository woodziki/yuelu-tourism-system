<template>
  <div class="detail-page">
    <Navbar />

    <!-- 顶部操作区：返回按钮 -->
    <div class="detail-header">
      <el-button type="text" icon="el-icon-arrow-left" @click="goBack">
        返回
      </el-button>
    </div>

    <!-- 主体内容 -->
    <div class="detail-main" v-if="spot">
      <div class="detail-card">
        <div class="detail-top">
          <!-- 左侧大图 -->
          <div class="detail-left">
            <div class="detail-cover">
              <img :src="spot.imageUrl || defaultCoverUrl" alt="景点大图">
            </div>
          </div>

          <!-- 右侧基本信息 -->
          <div class="detail-right">
            <h1 class="spot-name">{{ spot.name }}</h1>

            <div class="spot-meta">
              <div class="meta-item">
                <span class="meta-label">评分：</span>
                <el-rate
                  :value="rateValue"
                  disabled
                  show-score
                  text-color="#f59e0b"
                  score-template="{value} 分"
                />
              </div>

              <div class="meta-item" v-if="spot.duration">
                <span class="meta-label">建议时长：</span>
                <span class="meta-value">{{ spot.duration }}</span>
              </div>

              <div class="meta-item" v-if="spot.price != null">
                <span class="meta-label">门票 / 费用：</span>
                <span class="meta-value price">
                  ￥{{ spot.price }}
                </span>
              </div>

              <div class="meta-item">
                <span class="meta-label">标签：</span>
                <div class="tag-list">
                  <el-tag
                    v-for="tag in parseTags(spot.tags)"
                    :key="tag"
                    size="mini"
                    type="success"
                    effect="plain"
                  >
                    {{ tag }}
                  </el-tag>
                  <span
                    v-if="!parseTags(spot.tags).length"
                    class="tag-placeholder"
                  >
                    暂无标签
                  </span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="detail-actions">
              <el-button
                icon="el-icon-star-on"
                :loading="favLoading"
                @click="handleFavorite"
                :type="isFavorited ? 'info' : 'primary'"
                :plain="isFavorited"
              >
                {{ isFavorited ? '✅ 已收藏' : '⭐ 收藏景点' }}
              </el-button>
              <el-button
                type="default"
                icon="el-icon-edit"
                @click="handleWriteComment"
              >
                ✍️ 写评价
              </el-button>
            </div>
          </div>
        </div>

        <!-- 底部富文本内容 -->
        <div class="detail-content" v-if="spot.content">
          <h2 class="content-title">景点详情介绍</h2>
          <div
            class="content-body"
            v-html="spot.content"
          />
        </div>

        <!-- 游客评价列表 -->
        <div class="comment-section">
          <h2 class="comment-title">游客评价</h2>
          <div v-if="commentLoading" class="comment-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else>
            <div v-if="comments.length">
              <el-card
                v-for="item in comments"
                :key="item.id"
                class="comment-card"
                shadow="never"
              >
                <div class="comment-header">
                  <span class="comment-nickname">{{ item.nickname || '匿名用户' }}</span>
                  <span class="comment-time">{{ item.time || '时间未知' }}</span>
                </div>
                <div class="comment-rate">
                  <el-rate
                    :value="item.star"
                    disabled
                    text-color="#f59e0b"
                  />
                </div>
                <p class="comment-content">
                  {{ item.content || '（此用户未填写文字评价）' }}
                </p>
              </el-card>
            </div>
            <div v-else class="comment-empty">
              <el-empty description="还没有人留下评价，快来抢沙发吧～" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据加载失败或为空时的兜底 -->
    <div v-else class="detail-empty">
      <el-empty description="未找到该景点信息，请返回重试～" />
    </div>

    <!-- 撰写评价弹窗 -->
    <el-dialog
      title="撰写评价"
      :visible.sync="commentDialogVisible"
      width="520px"
    >
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate
            v-model="commentForm.star"
            show-text
            :max="5"
          />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            placeholder="可以从景色、交通、体验等方面简单写几句感受～"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="commentDialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          :loading="commentSubmitting"
          @click="submitComment"
        >
          提 交
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import request from '@/utils/request'

export default {
  name: 'SpotDetail',

  components: {
    Navbar
  },

  data () {
    return {
      spot: null, // 景点详情对象
      defaultCoverUrl: 'https://api.dujin.org/bing/1920.php',
      favLoading: false,
      // 当前景点是否已被当前登录用户收藏（由 /favorite/check 决定）
      isFavorited: false,
      // 评论相关状态
      comments: [],
      commentLoading: false,
      commentDialogVisible: false,
      commentSubmitting: false,
      commentForm: {
        star: 0,
        content: ''
      }
    }
  },

  computed: {
    /**
     * 将后端的 score 映射为 0~5 的评分值，用于 el-rate 展示。
     */
    rateValue () {
      if (!this.spot || this.spot.score == null) {
        return 0
      }
      const v = Number(this.spot.score)
      if (isNaN(v)) return 0
      // 防御性处理：保证在 0~5 区间内
      return Math.max(0, Math.min(5, v))
    }
  },

  mounted () {
    this.fetchDetail()
  },

  methods: {
    /**
     * 根据路由参数加载景点详情。
     *
     * 路由格式：/spot/:id
     * 实际接口：GET /spot/{id}
     */
    fetchDetail () {
      const id = this.$route.params.id
      if (!id) {
        this.$message.error('缺少景点 ID')
        return
      }

      request({
        url: '/spot/' + id,
        method: 'get'
      })
        .then(res => {
          // res 即为 Result<T>.data（Spot 对象）
          this.spot = res || null
          // 获取到景点信息后，立即查询当前用户的收藏状态与评论列表
          if (this.spot && this.spot.id) {
            this.checkFavoriteStatus()
            this.fetchComments()
          }
        })
        .catch(err => {
          console.error('加载景点详情失败：', err)
          this.spot = null
        })
    },

    /**
     * 加载当前景点的评论列表。
     *
     * 接口：GET /comment/list?spotId=xxx
     */
    fetchComments () {
      if (!this.spot || !this.spot.id) return

      this.commentLoading = true
      request({
        url: '/comment/list',
        method: 'get',
        params: {
          spotId: this.spot.id
        }
      })
        .then(res => {
          this.comments = Array.isArray(res) ? res : []
        })
        .catch(err => {
          console.error('加载评论列表失败：', err)
          this.comments = []
        })
        .finally(() => {
          this.commentLoading = false
        })
    },

    /**
     * 查询当前用户是否已收藏当前景点。
     *
     * <p>接口：GET /favorite/check?spotId=xxx</p>
     * <p>说明：Token 会由 request.js 的请求拦截器自动带上；若未登录/Token 失效，
     * 会被响应拦截器统一处理（清 token + 跳转 /login）。</p>
     */
    checkFavoriteStatus () {
      if (!this.spot || !this.spot.id) return

      request({
        url: '/favorite/check',
        method: 'get',
        params: {
          spotId: this.spot.id
        }
      })
        .then(res => {
          // res 为 boolean
          this.isFavorited = !!res
        })
        .catch(err => {
          console.error('查询收藏状态失败：', err)
          // 查询失败时不阻断页面展示，默认视为未收藏
          this.isFavorited = false
        })
    },

    /**
     * 解析标签字符串为数组。
     */
    parseTags (tagsStr) {
      if (!tagsStr) return []
      return tagsStr
        .split(',')
        .map(t => t.trim())
        .filter(Boolean)
    },

    /**
     * 返回上一页（若无历史，则回到首页）。
     */
    goBack () {
      if (window.history.length > 1) {
        this.$router.back()
      } else {
        this.$router.push('/')
      }
    },

    /**
     * 收藏当前景点。
     *
     * <p>实现“收藏/取消收藏”状态切换：</p>
     * <ul>
     *   <li>未收藏时：POST /favorite/add?spotId=xxx，成功后 isFavorited=true</li>
     *   <li>已收藏时：POST /favorite/remove?spotId=xxx，成功后 isFavorited=false</li>
     * </ul>
     */
    handleFavorite () {
      if (!this.spot || !this.spot.id) return

      this.favLoading = true
      request({
        url: this.isFavorited ? '/favorite/remove' : '/favorite/add',
        method: 'post',
        params: {
          spotId: this.spot.id
        }
      })
        .then(() => {
          if (this.isFavorited) {
            this.isFavorited = false
            this.$message.success('已取消收藏')
          } else {
            this.isFavorited = true
            this.$message.success('收藏成功，算法已记录您的偏好！')
          }
        })
        .catch(err => {
          console.error('收藏失败：', err)
          // 具体错误已在拦截器中提示，这里仅兜底
        })
        .finally(() => {
          this.favLoading = false
        })
    },

    handleWriteComment () {
      if (!this.spot || !this.spot.id) return
      this.commentDialogVisible = true
    },

    /**
     * 提交评价。
     *
     * 接口：POST /comment/add
     */
    submitComment () {
      if (!this.spot || !this.spot.id) return
      if (!this.commentForm.star) {
        this.$message.warning('请先给出评分（至少 1 星）')
        return
      }
      if (!this.commentForm.content || !this.commentForm.content.trim()) {
        this.$message.warning('请填写评价内容')
        return
      }

      this.commentSubmitting = true
      request({
        url: '/comment/add',
        method: 'post',
        data: {
          spotId: this.spot.id,
          star: this.commentForm.star,
          content: this.commentForm.content
        }
      })
        .then(() => {
          this.$message.success('评价已提交，感谢你的反馈！')
          this.commentDialogVisible = false
          this.commentForm.star = 0
          this.commentForm.content = ''
          // 重新加载评论列表
          this.fetchComments()
        })
        .catch(err => {
          console.error('提交评价失败：', err)
        })
        .finally(() => {
          this.commentSubmitting = false
        })
    }
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding: 16px 24px 32px;
  box-sizing: border-box;
}

/* 顶部返回按钮区域 */
.detail-header {
  max-width: 1200px;
  margin: 0 auto 8px;
}

/* 主体卡片容器 */
.detail-main {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-card {
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow:
    0 10px 25px rgba(15, 23, 42, 0.08),
    0 0 0 1px rgba(209, 213, 219, 0.6);
  padding: 20px 20px 24px;
  box-sizing: border-box;
}

/* 上半部分左右布局 */
.detail-top {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

/* 左侧大图 */
.detail-left {
  flex: 1 1 45%;
  min-width: 280px;
}

.detail-cover {
  width: 100%;
  height: 260px;
  border-radius: 12px;
  overflow: hidden;
  background-color: #e5e7eb;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.6s ease;
}

.detail-cover:hover img {
  transform: scale(1.05);
}

/* 右侧信息 */
.detail-right {
  flex: 1 1 50%;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.spot-name {
  margin: 4px 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

/* 元信息区域 */
.spot-meta {
  margin-top: 4px;
}

.meta-item {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.meta-label {
  font-size: 13px;
  color: #6b7280;
  margin-right: 4px;
}

.meta-value {
  font-size: 14px;
  color: #111827;
}

.meta-value.price {
  color: #16a34a;
  font-weight: 600;
}

/* 标签区域 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-placeholder {
  font-size: 12px;
  color: #9ca3af;
}

/* 操作按钮区域 */
.detail-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

/* 底部富文本内容 */
.detail-content {
  margin-top: 20px;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

.content-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.content-body {
  font-size: 14px;
  line-height: 1.7;
  color: #374151;
}

/* 评论区样式 */
.comment-section {
  margin-top: 24px;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

.comment-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.comment-card {
  margin-bottom: 12px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.comment-nickname {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.comment-time {
  font-size: 12px;
  color: #9ca3af;
}

.comment-rate {
  margin-bottom: 4px;
}

.comment-content {
  margin: 0;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
}

.comment-empty {
  margin-top: 8px;
}

/* 兜底空状态 */
.detail-empty {
  max-width: 1200px;
  margin: 40px auto;
}

/* 小屏适配：上下布局 */
@media (max-width: 768px) {
  .detail-page {
    padding: 12px 12px 24px;
  }

  .detail-card {
    padding: 14px 14px 20px;
    border-radius: 12px;
  }

  .detail-top {
    flex-direction: column;
  }

  .detail-cover {
    height: 220px;
  }

  .spot-name {
    font-size: 20px;
  }
}
</style>