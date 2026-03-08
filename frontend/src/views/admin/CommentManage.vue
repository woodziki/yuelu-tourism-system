<template>
  <div class="comment-manage">
    <el-card shadow="never">
      <!-- 顶部搜索区 -->
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="输入评论内容搜索"
          clearable
          size="small"
          prefix-icon="el-icon-search"
          class="search-input"
          @keyup.enter.native="handleSearch"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleSearch">
            搜索
          </el-button>
        </el-input>
      </div>

      <!-- 评论表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="90" align="center" />
        <el-table-column prop="spotName" label="景点名称" min-width="120" show-overflow-tooltip />
        <el-table-column label="用户昵称" width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" type="success">
              {{ scope.row.nickname || '未知用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="160" align="center">
          <template slot-scope="scope">
            <el-rate
              :value="scope.row.star"
              disabled
              show-score
              text-color="#f59e0b"
              score-template="{value} 分"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="评论时间" width="160" align="center">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime || scope.row.time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template slot-scope="scope">
            <el-popconfirm
              title="确定要删除这条违规评论吗？"
              @confirm="handleDelete(scope.row.id)"
            >
              <el-button slot="reference" type="danger" size="mini">
                删除
              </el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-wrap">
        <el-pagination
          :current-page="current"
          :page-sizes="[10, 20, 50]"
          :page-size="size"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="onSizeChange"
          @current-change="onCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'CommentManage',

  data () {
    return {
      loading: false,
      tableData: [],
      current: 1,
      size: 10,
      total: 0,
      keyword: ''
    }
  },

  mounted () {
    this.fetchData()
  },

  methods: {
    /**
     * 拉取后台评论分页数据。
     *
     * 接口：GET /comment/adminList
     */
    fetchData () {
      this.loading = true
      request({
        url: '/comment/adminList',
        method: 'get',
        params: {
          current: this.current,
          size: this.size,
          keyword: this.keyword || undefined
        }
      })
        .then(res => {
          this.tableData = (res && res.records) ? res.records : []
          this.total = (res && res.total) ? res.total : 0
        })
        .catch(() => {
          this.tableData = []
          this.total = 0
        })
        .finally(() => {
          this.loading = false
        })
    },

    /**
     * 搜索：重置到第 1 页并刷新数据。
     */
    handleSearch () {
      this.current = 1
      this.fetchData()
    },

    onSizeChange (val) {
      this.size = val
      this.current = 1
      this.fetchData()
    },

    onCurrentChange (val) {
      this.current = val
      this.fetchData()
    },

    /**
     * 删除评论：DELETE /comment/delete/{id}
     */
    handleDelete (id) {
      request({
        url: '/comment/delete/' + id,
        method: 'delete'
      })
        .then(() => {
          this.$message.success('删除成功')
          this.fetchData()
        })
        .catch(() => {})
    },

    /**
     * 时间格式化：后端通常已返回 yyyy-MM-dd HH:mm，这里做兜底处理。
     */
    formatTime (t) {
      return t || '-'
    }
  }
}
</script>

<style scoped>
.comment-manage {
  padding: 0;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.search-input {
  width: 360px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .toolbar {
    justify-content: flex-start;
  }
  .search-input {
    width: 100%;
  }
}
</style>