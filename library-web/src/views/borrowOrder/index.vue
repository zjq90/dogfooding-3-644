<template>
  <div class="borrow-order-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="订单编号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" clearable placeholder="请选择" style="width: 120px">
            <el-option label="借阅中" :value="0" />
            <el-option label="已归还" :value="1" />
            <el-option label="逾期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="押金状态">
          <el-select v-model="searchForm.depositStatus" clearable placeholder="请选择" style="width: 120px">
            <el-option label="未退还" :value="0" />
            <el-option label="已退还" :value="1" />
            <el-option label="已扣除" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.paymentStatus" clearable placeholder="请选择" style="width: 120px">
            <el-option label="未支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已退款" :value="2" />
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
        <span>借阅订单列表</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增借阅
        </el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="borrowerName" label="借阅人" width="100" />
        <el-table-column prop="borrowerPhone" label="手机号" width="130" />
        <el-table-column prop="bookTitle" label="图书名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="bookIsbn" label="ISBN" width="130" />
        <el-table-column prop="depositAmount" label="押金" width="90" align="right">
          <template slot-scope="scope">
            ¥{{ scope.row.depositAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="borrowDate" label="借阅日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column prop="returnDate" label="归还日期" width="120">
          <template slot-scope="scope">
            {{ scope.row.returnDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusColor(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="depositStatus" label="押金状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="getDepositStatusColor(scope.row.depositStatus)" size="small">
              {{ getDepositStatusText(scope.row.depositStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="getPaymentStatusColor(scope.row.paymentStatus)" size="small">
              {{ getPaymentStatusText(scope.row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="90" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.overdueDays > 0" class="text-danger">
              {{ scope.row.overdueDays }}天
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="overdueFine" label="逾期罚款" width="90" align="right">
          <template slot-scope="scope">
            <span v-if="scope.row.overdueFine > 0" class="text-danger">
              ¥{{ scope.row.overdueFine }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template slot-scope="scope">
            <el-button 
              type="success" 
              size="small" 
              @click="handleReturn(scope.row)"
              :disabled="scope.row.status === 1"
            >
              归还
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleRefund(scope.row)"
              :disabled="scope.row.depositStatus !== 0"
            >
              退押金
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
      title="新增借阅"
      :visible.sync="dialogVisible"
      width="500px"
    >
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="借阅人员" prop="borrowerId">
          <el-select
            v-model="form.borrowerId"
            filterable
            remote
            reserve-keyword
            placeholder="请选择借阅人员"
            style="width: 100%"
            :remote-method="searchBorrower"
            :loading="borrowerLoading"
          >
            <el-option
              v-for="item in borrowerOptions"
              :key="item.id"
              :label="item.name + ' - ' + item.phone"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图书" prop="bookId">
          <el-select
            v-model="form.bookId"
            filterable
            remote
            reserve-keyword
            placeholder="请选择图书"
            style="width: 100%"
            :remote-method="searchBook"
            :loading="bookLoading"
          >
            <el-option
              v-for="item in bookOptions"
              :key="item.id"
              :label="item.title + ' - ' + item.isbn"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅天数" prop="borrowDays">
          <el-input-number v-model="form.borrowDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="押金金额" prop="depositAmount">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入操作人" />
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
  </div>
</template>

<script>
import { 
  getBorrowOrderList, 
  createBorrowOrder, 
  returnBook, 
  refundOrderDeposit,
  deleteBorrowOrder
} from '@/api/borrowOrder'
import { getAllBorrowers } from '@/api/borrower'
import { getBookList } from '@/api/book'

export default {
  name: 'BorrowOrder',
  data() {
    return {
      loading: false,
      submitLoading: false,
      borrowerLoading: false,
      bookLoading: false,
      searchForm: {
        keyword: '',
        status: null,
        depositStatus: null,
        paymentStatus: null
      },
      tableData: [],
      page: 1,
      size: 10,
      total: 0,
      dialogVisible: false,
      form: {
        borrowerId: null,
        bookId: null,
        borrowDays: 30,
        depositAmount: 50,
        paymentStatus: 1,
        operator: 'admin',
        remark: ''
      },
      borrowerOptions: [],
      bookOptions: [],
      rules: {
        borrowerId: [{ required: true, message: '请选择借阅人员', trigger: 'change' }],
        bookId: [{ required: true, message: '请选择图书', trigger: 'change' }]
      }
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    getStatusText(status) {
      const map = { 0: '借阅中', 1: '已归还', 2: '逾期' }
      return map[status] || '未知'
    },
    getStatusColor(status) {
      const map = { 0: 'primary', 1: 'success', 2: 'danger' }
      return map[status] || 'info'
    },
    getDepositStatusText(status) {
      const map = { 0: '未退还', 1: '已退还', 2: '已扣除' }
      return map[status] || '未知'
    },
    getDepositStatusColor(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'danger' }
      return map[status] || 'info'
    },
    getPaymentStatusText(status) {
      const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
      return map[status] || '未知'
    },
    getPaymentStatusColor(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'info' }
      return map[status] || 'info'
    },
    fetchData() {
      this.loading = true
      const params = {
        page: this.page,
        size: this.size,
        keyword: this.searchForm.keyword,
        status: this.searchForm.status,
        depositStatus: this.searchForm.depositStatus,
        paymentStatus: this.searchForm.paymentStatus
      }
      getBorrowOrderList(params).then(res => {
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
        status: null,
        depositStatus: null,
        paymentStatus: null
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
      this.form = {
        borrowerId: null,
        bookId: null,
        borrowDays: 30,
        depositAmount: 50,
        paymentStatus: 1,
        operator: 'admin',
        remark: ''
      }
      this.dialogVisible = true
    },
    handleReturn(row) {
      this.$confirm('确认归还该图书吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        returnBook(row.id).then(() => {
          this.$message.success('图书归还成功')
          this.fetchData()
        }).catch(err => {
          this.$message.error(err.message || '归还失败')
        })
      })
    },
    handleRefund(row) {
      this.$confirm('确认退还该订单押金吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        refundOrderDeposit(row.id).then(() => {
          this.$message.success('押金退还成功')
          this.fetchData()
        }).catch(err => {
          this.$message.error(err.message || '退还失败')
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该订单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteBorrowOrder(row.id).then(() => {
          this.$message.success('删除成功')
          this.fetchData()
        }).catch(err => {
          this.$message.error(err.message || '删除失败')
        })
      })
    },
    searchBorrower(query) {
      if (query) {
        this.borrowerLoading = true
        getAllBorrowers().then(res => {
          this.borrowerOptions = res.data.filter(item => 
            item.name.includes(query) || item.phone.includes(query)
          )
          this.borrowerLoading = false
        })
      } else {
        this.borrowerOptions = []
      }
    },
    searchBook(query) {
      if (query) {
        this.bookLoading = true
        getBookList({ keyword: query, page: 1, size: 20 }).then(res => {
          this.bookOptions = res.data.records
          this.bookLoading = false
        })
      } else {
        this.bookOptions = []
      }
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          createBorrowOrder(this.form).then(() => {
            this.$message.success('借阅订单创建成功')
            this.dialogVisible = false
            this.fetchData()
            this.submitLoading = false
          }).catch(err => {
            this.$message.error(err.message || '创建失败')
            this.submitLoading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.borrow-order-container {
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
.text-danger {
  color: #f56c6c;
  font-weight: bold;
}
</style>
