<template>
  <div class="user-manage">
    <el-card shadow="never">
      <!-- 顶部搜索区 -->
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="输入用户名或昵称搜索"
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

      <!-- 用户表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="100" align="center" sortable />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column label="昵称" min-width="120">
          <template slot-scope="scope">
            <el-tag size="small" type="primary">
              {{ scope.row.nickname || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180" align="center">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
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
  name: 'UserManage',

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
     * 拉取后台用户分页数据。
     * 接口：GET /user/adminList
     */
    fetchData () {
      this.loading = true
      request({
        url: '/user/adminList',
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
     * 时间格式化：YYYY-MM-DD HH:mm:ss
     */
    formatTime (t) {
      if (!t) return '-'
      const d = new Date(t)
      if (isNaN(d.getTime())) return '-'
      const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }
  }
}
</script>

<style scoped>
.user-manage {
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
