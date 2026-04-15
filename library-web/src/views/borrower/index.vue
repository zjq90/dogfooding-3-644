<template>
  <div class="borrower-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="姓名/借阅证号/手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable placeholder="请选择状态" style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
            <el-option label="挂失" :value="2" />
          </el-select>
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
        <span>借阅人员列表</span>
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
        <el-table-column prop="borrowerNo" label="借阅证号" width="120" align="center" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template slot-scope="scope">
            {{ scope.row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="depositAmount" label="押金金额" width="100" align="right">
          <template slot-scope="scope">
            ¥{{ scope.row.depositAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="账户余额" width="100" align="right">
          <template slot-scope="scope">
            ¥{{ scope.row.balance }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="success" size="small" @click="handleDeposit(scope.row)">
              押金
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
        <el-form-item label="借阅证号" prop="borrowerNo">
          <el-input v-model="form.borrowerNo" placeholder="请输入借阅证号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="联系地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入联系地址" />
        </el-form-item>
        <el-form-item label="押金金额" prop="depositAmount">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
            <el-radio :label="2">挂失</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入备注" />
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
      title="押金操作"
      :visible.sync="depositDialogVisible"
      width="450px"
    >
      <el-form
        ref="depositForm"
        :model="depositForm"
        :rules="depositRules"
        label-width="100px"
      >
        <el-form-item label="操作类型" prop="type">
          <el-radio-group v-model="depositForm.type">
            <el-radio :label="1">缴纳</el-radio>
            <el-radio :label="2">退还</el-radio>
            <el-radio :label="3">扣除</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="depositForm.amount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod" v-if="depositForm.type !== 3">
          <el-select v-model="depositForm.paymentMethod" placeholder="请选择">
            <el-option label="现金" :value="1" />
            <el-option label="微信" :value="2" />
            <el-option label="支付宝" :value="3" />
            <el-option label="银行卡" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="depositForm.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="depositDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="depositSubmitLoading" @click="handleDepositSubmit">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getBorrowerList, 
  addBorrower, 
  updateBorrower, 
  deleteBorrower 
} from '@/api/borrower'
import { addDeposit, refundDeposit, deductDeposit } from '@/api/deposit'

export default {
  name: 'Borrower',
  data() {
    return {
      loading: false,
      submitLoading: false,
      depositSubmitLoading: false,
      searchForm: {
        keyword: '',
        status: null
      },
      tableData: [],
      page: 1,
      size: 10,
      total: 0,
      dialogVisible: false,
      depositDialogVisible: false,
      dialogTitle: '新增人员',
      form: {
        id: null,
        borrowerNo: '',
        name: '',
        gender: 1,
        phone: '',
        email: '',
        idCard: '',
        address: '',
        depositAmount: 0,
        status: 1,
        remark: ''
      },
      currentBorrower: null,
      depositForm: {
        type: 1,
        amount: 0,
        paymentMethod: 1,
        remark: ''
      },
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
      },
      depositRules: {
        amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    getStatusText(status) {
      const map = { 0: '禁用', 1: '正常', 2: '挂失' }
      return map[status] || '未知'
    },
    getStatusType(status) {
      const map = { 0: 'danger', 1: 'success', 2: 'warning' }
      return map[status] || 'info'
    },
    fetchData() {
      this.loading = true
      const params = {
        page: this.page,
        size: this.size,
        keyword: this.searchForm.keyword,
        status: this.searchForm.status
      }
      getBorrowerList(params).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.page = 1
      this.fetchData()
    },
    handleReset() {
      this.searchForm = {
        keyword: '',
        status: null
      }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.size = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.page = val
      this.fetchData()
    },
    handleAdd() {
      this.dialogTitle = '新增人员'
      this.form = {
        id: null,
        borrowerNo: '',
        name: '',
        gender: 1,
        phone: '',
        email: '',
        idCard: '',
        address: '',
        depositAmount: 0,
        status: 1,
        remark: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑人员'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDeposit(row) {
      this.currentBorrower = row
      this.depositForm = {
        type: 1,
        amount: 0,
        paymentMethod: 1,
        remark: ''
      }
      this.depositDialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确认删除该借阅人员吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteBorrower(row.id).then(() => {
          this.$message.success('删除成功')
          this.fetchData()
        }).catch(err => {
          this.$message.error(err.message || '删除失败')
        })
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          const request = this.form.id 
            ? updateBorrower(this.form.id, this.form)
            : addBorrower(this.form)
          
          request.then(() => {
            this.$message.success(this.form.id ? '更新成功' : '添加成功')
            this.dialogVisible = false
            this.fetchData()
            this.submitLoading = false
          }).catch(err => {
            this.$message.error(err.message || '操作失败')
            this.submitLoading = false
          })
        }
      })
    },
    handleDepositSubmit() {
      this.$refs.depositForm.validate(valid => {
        if (valid) {
          this.depositSubmitLoading = true
          const data = {
            borrowerId: this.currentBorrower.id,
            amount: this.depositForm.amount,
            paymentMethod: this.depositForm.paymentMethod,
            remark: this.depositForm.remark,
            operator: 'admin'
          }
          
          let request
          if (this.depositForm.type === 1) {
            request = addDeposit(data)
          } else if (this.depositForm.type === 2) {
            request = refundDeposit(data)
          } else {
            request = deductDeposit(data)
          }
          
          request.then(() => {
            this.$message.success('操作成功')
            this.depositDialogVisible = false
            this.fetchData()
            this.depositSubmitLoading = false
          }).catch(err => {
            this.$message.error(err.message || '操作失败')
            this.depositSubmitLoading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.borrower-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.table-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
