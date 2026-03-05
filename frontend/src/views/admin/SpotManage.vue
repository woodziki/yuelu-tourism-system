<template>
  <div class="spot-manage">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
            + 新增景点
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="searchName"
            placeholder="输入景点名称搜索"
            clearable
            size="small"
            prefix-icon="el-icon-search"
            @keyup.enter.native="handleSearch"
            class="search-input"
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
        <el-table-column label="封面图" width="80" align="center">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.imageUrl"
              :src="scope.row.imageUrl"
              :preview-src-list="[scope.row.imageUrl]"
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 6px;"
            />
            <span v-else class="no-image">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="景点名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" min-width="120" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="90" align="right">
          <template slot-scope="scope">
            {{ scope.row.price != null ? '￥' + scope.row.price : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="游览时长" width="100" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定删除该景点吗？"
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
      :title="form.id ? '编辑景点' : '新增景点'"
      :visible.sync="dialogVisible"
      width="560px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入景点名称" />
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入封面图 URL" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用英文逗号分隔，如：人文,古迹"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            placeholder="门票/费用"
          />
        </el-form-item>
        <el-form-item label="时长" prop="duration">
          <el-input v-model="form.duration" placeholder="如：2小时" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input
            v-model="form.intro"
            type="textarea"
            :rows="3"
            placeholder="简短介绍"
          />
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
  name: 'SpotManage',

  data () {
    return {
      loading: false,
      tableData: [],
      current: 1,
      size: 10,
      total: 0,
      // 搜索关键字（按名称模糊查询）
      searchName: '',

      dialogVisible: false,
      submitLoading: false,
      form: {
        id: null,
        name: '',
        imageUrl: '',
        tags: '',
        price: null,
        duration: '',
        intro: '',
        content: '',
        viewCount: null,
        score: null
      },
      formRules: {
        name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }]
      }
    }
  },

  mounted () {
    this.fetchData()
  },

  methods: {
    /**
     * 获取分页列表数据。
     * 调用 GET /spot/list，赋值 tableData 与 total。
     */
    fetchData () {
      this.loading = true
      request({
        url: '/spot/adminList',
        method: 'get',
        params: {
          current: this.current,
          size: this.size,
          name: this.searchName || undefined
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
     * 执行搜索：重置页码到第 1 页，再拉取数据。
     */
    handleSearch () {
      this.current = 1
      this.fetchData()
    },

    /**
     * 新增：清空表单并打开弹窗。
     */
    handleAdd () {
      this.form = {
        id: null,
        name: '',
        imageUrl: '',
        tags: '',
        price: null,
        duration: '',
        intro: '',
        content: '',
        viewCount: null,
        score: null
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef && this.$refs.formRef.clearValidate()
      })
    },

    /**
     * 编辑：将当前行数据深拷贝到表单并打开弹窗。
     */
    handleEdit (row) {
      this.form = {
        id: row.id,
        name: row.name || '',
        imageUrl: row.imageUrl || '',
        tags: row.tags || '',
        price: row.price != null ? Number(row.price) : null,
        duration: row.duration || '',
        intro: row.intro || '',
        content: row.content || '',
        viewCount: row.viewCount != null ? row.viewCount : null,
        score: row.score != null ? Number(row.score) : null
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef && this.$refs.formRef.clearValidate()
      })
    },

    /**
     * 删除：调用 DELETE /spot/delete/{id}，成功后刷新列表。
     */
    handleDelete (id) {
      request({
        url: '/spot/delete/' + id,
        method: 'delete'
      })
        .then(() => {
          this.$message.success('删除成功')
          this.fetchData()
        })
        .catch(() => {})
    },

    /**
     * 提交表单：有 id 则 PUT 更新，无 id 则 POST 新增；成功后关闭弹窗并刷新列表。
     */
    submitForm () {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        const isEdit = !!this.form.id
        const url = isEdit ? '/spot/update' : '/spot/add'
        const method = isEdit ? 'put' : 'post'
        request({
          url,
          method,
          data: this.form
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
        imageUrl: '',
        tags: '',
        price: null,
        duration: '',
        intro: '',
        content: '',
        viewCount: null,
        score: null
      }
    }
  }
}
</script>

<style scoped>
.spot-manage {
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
