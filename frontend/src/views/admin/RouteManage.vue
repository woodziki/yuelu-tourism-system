<template>
  <div class="route-manage">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
            + 新增线路
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="keyword"
            placeholder="输入线路名称搜索"
            clearable
            size="small"
            prefix-icon="el-icon-search"
            class="search-input"
            @keyup.enter.native="handleSearch"
          >
            <el-button slot="append" icon="el-icon-search" @click="handleSearch" />
          </el-input>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="90" align="center" sortable />
        <el-table-column prop="name" label="线路名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="intro" label="简介" min-width="180" show-overflow-tooltip />
        <el-table-column label="地图横幅" width="100" align="center">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.mapImg"
              :src="scope.row.mapImg"
              :preview-src-list="[scope.row.mapImg]"
              fit="cover"
              style="width: 60px; height: 40px; border-radius: 4px;"
            />
            <span v-else class="no-image">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定删除该线路吗？"
              @confirm="handleDelete(scope.row.id)"
            >
              <el-button slot="reference" type="text" size="small" style="color: #f56c6c;">
                删除
              </el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="form.id ? '编辑线路' : '新增线路'"
      :visible.sync="dialogVisible"
      width="620px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入线路名称" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input
            v-model="form.intro"
            type="textarea"
            :rows="3"
            placeholder="线路简介"
          />
        </el-form-item>
        <el-form-item label="横幅地址" prop="mapImg">
          <el-input v-model="form.mapImg" placeholder="请输入地图/横幅图片 URL" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用英文逗号分隔，如：人文,古迹"
          />
        </el-form-item>
        <el-form-item label="关联景点" prop="spotIds">
          <el-select
            v-model="form.spotIds"
            multiple
            filterable
            placeholder="请选择并排列景点(按选择顺序作为游玩顺序)"
            style="width: 100%"
          >
            <el-option
              v-for="s in spotList"
              :key="s.id"
              :label="s.name"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确 定
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'RouteManage',

  data () {
    return {
      loading: false,
      tableData: [],
      current: 1,
      size: 10,
      total: 0,
      keyword: '',

      spotList: [],
      dialogVisible: false,
      submitLoading: false,
      form: {
        id: null,
        name: '',
        intro: '',
        mapImg: '',
        tags: '',
        spotIds: []
      },
      formRules: {
        name: [{ required: true, message: '请输入线路名称', trigger: 'blur' }]
      }
    }
  },

  mounted () {
    this.fetchData()
    this.fetchSpotList()
  },

  methods: {
    fetchData () {
      this.loading = true
      request({
        url: '/route/adminList',
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

    fetchSpotList () {
      request({
        url: '/spot/adminList',
        method: 'get',
        params: { current: 1, size: 500 }
      })
        .then(res => {
          this.spotList = (res && res.records) ? res.records : []
        })
        .catch(() => {
          this.spotList = []
        })
    },

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

    handleAdd () {
      this.form = {
        id: null,
        name: '',
        intro: '',
        mapImg: '',
        tags: '',
        spotIds: []
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef && this.$refs.formRef.clearValidate()
      })
    },

    handleEdit (row) {
      this.form = {
        id: row.id,
        name: row.name || '',
        intro: row.intro || '',
        mapImg: row.mapImg || '',
        tags: row.tags || '',
        spotIds: []
      }
      request({
        url: '/route/' + row.id + '/spotIds',
        method: 'get'
      })
        .then(spotIds => {
          this.form.spotIds = Array.isArray(spotIds) ? spotIds : []
          this.dialogVisible = true
          this.$nextTick(() => {
            this.$refs.formRef && this.$refs.formRef.clearValidate()
          })
        })
        .catch(() => {
          this.form.spotIds = []
          this.dialogVisible = true
          this.$nextTick(() => {
            this.$refs.formRef && this.$refs.formRef.clearValidate()
          })
        })
    },

    handleDelete (id) {
      request({
        url: '/route/delete/' + id,
        method: 'delete'
      })
        .then(() => {
          this.$message.success('删除成功')
          this.fetchData()
        })
        .catch(() => {})
    },

    submitForm () {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        const isEdit = !!this.form.id
        const url = isEdit ? '/route/update' : '/route/add'
        const method = isEdit ? 'put' : 'post'
        const dto = {
          id: this.form.id || undefined,
          name: this.form.name,
          intro: this.form.intro,
          mapImg: this.form.mapImg,
          tags: this.form.tags,
          spotIds: this.form.spotIds || []
        }
        request({
          url,
          method,
          data: dto
        })
          .then(() => {
            this.$message.success(isEdit ? '更新成功' : '新增成功')
            this.dialogVisible = false
            this.fetchData()
          })
          .catch(() => {})
          .finally(() => {
            this.submitLoading = false
          })
      })
    },

    resetForm () {
      this.form = {
        id: null,
        name: '',
        intro: '',
        mapImg: '',
        tags: '',
        spotIds: []
      }
    }
  }
}
</script>

<style scoped>
.route-manage {
  padding: 0;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.search-input {
  width: 260px;
}

.no-image {
  font-size: 12px;
  color: #909399;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
