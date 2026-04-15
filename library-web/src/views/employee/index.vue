<template>
  <div class="employee-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="姓名/工号/手机号/邮箱"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-cascader
            v-model="searchForm.departmentId"
            :options="departmentOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'name', children: 'children', emitPath: false }"
            clearable
            placeholder="请选择部门"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>内部人员列表</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增人员
        </el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="employeeNo" label="工号" width="100" align="center" />
        <el-table-column prop="departmentName" label="所属部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
    
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="550px"
    >
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="form.employeeNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="所属部门" prop="departmentId">
          <el-cascader
            v-model="form.departmentId"
            :options="departmentOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'name', children: 'children', emitPath: false }"
            clearable
            placeholder="请选择所属部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">离职</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getEmployeeList, 
  addEmployee, 
  updateEmployee, 
  deleteEmployee 
} from '@/api/employee'
import { getDepartmentTree } from '@/api/department'

export default {
  name: 'EmployeeManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      page: 1,
      size: 10,
      total: 0,
      searchForm: {
        keyword: '',
        departmentId: null
      },
      dialogVisible: false,
      dialogTitle: '新增人员',
      submitLoading: false,
      form: {
        id: null,
        name: '',
        employeeNo: '',
        departmentId: null,
        position: '',
        phone: '',
        email: '',
        status: 1
      },
      rules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
        ],
        employeeNo: [
          { required: true, message: '请输入工号', trigger: 'blur' }
        ],
        departmentId: [
          { required: true, message: '请选择所属部门', trigger: 'change' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      },
      departmentOptions: []
    }
  },
  created() {
    this.loadData()
    this.loadDepartmentTree()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getEmployeeList({
          page: this.page,
          size: this.size,
          keyword: this.searchForm.keyword,
          departmentId: this.searchForm.departmentId
        })
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      } catch (error) {
        console.error('加载人员失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    async loadDepartmentTree() {
      try {
        const res = await getDepartmentTree()
        if (res.code === 200) {
          this.departmentOptions = res.data
        }
      } catch (error) {
        console.error('加载部门树失败:', error)
      }
    },
    
    handleSearch() {
      this.page = 1
      this.loadData()
    },
    
    handleReset() {
      this.searchForm = {
        keyword: '',
        departmentId: null
      }
      this.handleSearch()
    },
    
    handleSizeChange(val) {
      this.size = val
      this.loadData()
    },
    
    handleCurrentChange(val) {
      this.page = val
      this.loadData()
    },
    
    handleAdd() {
      this.dialogTitle = '新增人员'
      this.form = {
        id: null,
        name: '',
        employeeNo: '',
        departmentId: null,
        position: '',
        phone: '',
        email: '',
        status: 1
      }
      this.dialogVisible = true
    },
    
    handleEdit(row) {
      this.dialogTitle = '编辑人员'
      this.form = { 
        id: row.id,
        name: row.name,
        employeeNo: row.employeeNo,
        departmentId: row.departmentId,
        position: row.position,
        phone: row.phone,
        email: row.email,
        status: row.status
      }
      this.dialogVisible = true
    },
    
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除人员「${row.name}」吗？`, '提示', {
          type: 'warning'
        })
        
        const res = await deleteEmployee(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadData()
        } else {
          this.$message.error(res.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    },
    
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            const api = this.form.id ? updateEmployee(this.form.id, this.form) : addEmployee(this.form)
            const res = await api
            
            if (res.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(res.message || '操作失败')
            }
          } catch (error) {
            console.error('提交失败:', error)
          } finally {
            this.submitLoading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.employee-container {
  padding-bottom: 24px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
}
</style>
