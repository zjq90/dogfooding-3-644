<template>
  <div class="department-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="部门名称/编号/描述"
            clearable
            style="width: 220px"
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
        <span>部门列表</span>
        <div>
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
            新增部门
          </el-button>
          <el-button type="info" icon="el-icon-setting" @click="handlePermissionConfig">
            权限配置
          </el-button>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        row-key="id"
        border
        default-expand-all
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        style="width: 100%"
      >
        <el-table-column prop="name" label="部门名称" min-width="180" />
        <el-table-column prop="code" label="部门编号" width="120" align="center" />
        <el-table-column prop="parentName" label="上级部门" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
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
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编号" prop="code">
          <el-input v-model="form.code" placeholder="格式：B-数字">
            <el-button slot="append" @click="generateCode">自动生成</el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="上级部门" prop="parentId">
          <el-cascader
            v-model="form.parentId"
            :options="departmentOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'name', children: 'children', emitPath: false }"
            clearable
            placeholder="请选择上级部门（不选则为顶级部门）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入部门描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </el-dialog>
    
    <el-dialog
      title="部门权限配置"
      :visible.sync="permissionDialogVisible"
      width="700px"
    >
      <el-form label-width="100px">
        <el-form-item label="选择部门">
          <el-cascader
            v-model="selectedDepartmentId"
            :options="departmentOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'name', children: 'children', emitPath: false }"
            clearable
            placeholder="请选择部门"
            style="width: 100%"
            @change="handleDepartmentChange"
          />
        </el-form-item>
        <el-form-item label="菜单权限" v-if="selectedDepartmentId">
          <el-tree
            ref="menuTree"
            :data="menuPermissions"
            show-checkbox
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            default-expand-all
          />
        </el-form-item>
        <el-form-item label="按钮权限" v-if="selectedDepartmentId">
          <el-tree
            ref="buttonTree"
            :data="buttonPermissions"
            show-checkbox
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            default-expand-all
          />
        </el-form-item>
        <el-form-item label="信息权限" v-if="selectedDepartmentId">
          <el-tree
            ref="infoTree"
            :data="infoPermissions"
            show-checkbox
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            default-expand-all
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionLoading" @click="handleSavePermissions">
          保存权限
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getDepartmentList, 
  getDepartmentTree, 
  addDepartment, 
  updateDepartment, 
  deleteDepartment,
  updateDepartmentStatus,
  generateDepartmentCode
} from '@/api/department'
import { 
  getPermissionTree, 
  getDepartmentPermissionIds, 
  assignDepartmentPermissions 
} from '@/api/permission'

export default {
  name: 'DepartmentManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      page: 1,
      size: 10,
      total: 0,
      searchForm: {
        keyword: ''
      },
      dialogVisible: false,
      dialogTitle: '新增部门',
      submitLoading: false,
      form: {
        id: null,
        name: '',
        code: '',
        parentId: null,
        description: '',
        sortOrder: 0,
        status: 1
      },
      rules: {
        name: [
          { required: true, message: '请输入部门名称', trigger: 'blur' },
          { min: 2, max: 50, message: '部门名称长度为2-50个字符', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入部门编号', trigger: 'blur' },
          { pattern: /^B-\d+$/, message: '部门编号格式为B-数字', trigger: 'blur' }
        ]
      },
      departmentOptions: [],
      permissionDialogVisible: false,
      permissionLoading: false,
      selectedDepartmentId: null,
      menuPermissions: [],
      buttonPermissions: [],
      infoPermissions: []
    }
  },
  created() {
    this.loadData()
    this.loadDepartmentTree()
    this.loadPermissions()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getDepartmentList({
          page: this.page,
          size: this.size,
          keyword: this.searchForm.keyword
        })
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      } catch (error) {
        console.error('加载部门失败:', error)
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
    
    async loadPermissions() {
      try {
        const res = await getPermissionTree()
        if (res.code === 200) {
          const allPerms = res.data
          this.menuPermissions = allPerms.filter(p => p.type === 1)
          this.buttonPermissions = allPerms.filter(p => p.type === 2)
          this.infoPermissions = allPerms.filter(p => p.type === 3)
        }
      } catch (error) {
        console.error('加载权限失败:', error)
      }
    },
    
    handleSearch() {
      this.page = 1
      this.loadData()
    },
    
    handleReset() {
      this.searchForm = {
        keyword: ''
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
    
    async generateCode() {
      try {
        const res = await generateDepartmentCode()
        if (res.code === 200) {
          this.form.code = res.data
        }
      } catch (error) {
        console.error('生成部门编号失败:', error)
      }
    },
    
    handleAdd() {
      this.dialogTitle = '新增部门'
      this.form = {
        id: null,
        name: '',
        code: '',
        parentId: null,
        description: '',
        sortOrder: 0,
        status: 1
      }
      this.dialogVisible = true
    },
    
    handleEdit(row) {
      this.dialogTitle = '编辑部门'
      this.form = { 
        id: row.id,
        name: row.name,
        code: row.code,
        parentId: row.parentId || null,
        description: row.description,
        sortOrder: row.sortOrder,
        status: row.status
      }
      this.dialogVisible = true
    },
    
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除部门「${row.name}」吗？`, '提示', {
          type: 'warning'
        })
        
        const res = await deleteDepartment(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadData()
          this.loadDepartmentTree()
        } else {
          this.$message.error(res.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    },
    
    async handleStatusChange(row) {
      try {
        const res = await updateDepartmentStatus(row.id, row.status)
        if (res.code === 200) {
          this.$message.success('状态更新成功')
        } else {
          row.status = row.status === 1 ? 0 : 1
        }
      } catch (error) {
        row.status = row.status === 1 ? 0 : 1
        console.error('状态更新失败:', error)
      }
    },
    
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            const api = this.form.id ? updateDepartment(this.form.id, this.form) : addDepartment(this.form)
            const res = await api
            
            if (res.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.loadData()
              this.loadDepartmentTree()
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
    },
    
    handlePermissionConfig() {
      this.selectedDepartmentId = null
      this.permissionDialogVisible = true
    },
    
    async handleDepartmentChange(deptId) {
      if (!deptId) return
      
      try {
        const res = await getDepartmentPermissionIds(deptId)
        if (res.code === 200) {
          const permissionIds = res.data || []
          
          this.$nextTick(() => {
            if (this.$refs.menuTree) {
              this.$refs.menuTree.setCheckedKeys(permissionIds.filter(id => {
                const perm = this.findPermissionById(id, this.menuPermissions)
                return perm !== null
              }))
            }
            if (this.$refs.buttonTree) {
              this.$refs.buttonTree.setCheckedKeys(permissionIds.filter(id => {
                const perm = this.findPermissionById(id, this.buttonPermissions)
                return perm !== null
              }))
            }
            if (this.$refs.infoTree) {
              this.$refs.infoTree.setCheckedKeys(permissionIds.filter(id => {
                const perm = this.findPermissionById(id, this.infoPermissions)
                return perm !== null
              }))
            }
          })
        }
      } catch (error) {
        console.error('加载部门权限失败:', error)
      }
    },
    
    findPermissionById(id, permissions) {
      for (const perm of permissions) {
        if (perm.id === id) return perm
        if (perm.children) {
          const found = this.findPermissionById(id, perm.children)
          if (found) return found
        }
      }
      return null
    },
    
    async handleSavePermissions() {
      if (!this.selectedDepartmentId) {
        this.$message.warning('请选择部门')
        return
      }
      
      this.permissionLoading = true
      try {
        const menuIds = this.$refs.menuTree ? this.$refs.menuTree.getCheckedKeys() : []
        const buttonIds = this.$refs.buttonTree ? this.$refs.buttonTree.getCheckedKeys() : []
        const infoIds = this.$refs.infoTree ? this.$refs.infoTree.getCheckedKeys() : []
        
        const allPermissionIds = [...menuIds, ...buttonIds, ...infoIds]
        
        const res = await assignDepartmentPermissions(this.selectedDepartmentId, allPermissionIds)
        if (res.code === 200) {
          this.$message.success('权限配置成功')
          this.permissionDialogVisible = false
        } else {
          this.$message.error(res.message || '权限配置失败')
        }
      } catch (error) {
        console.error('保存权限失败:', error)
      } finally {
        this.permissionLoading = false
      }
    }
  }
}
</script>

<style scoped>
.department-container {
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
