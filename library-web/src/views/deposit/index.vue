<template>
  <div class="deposit-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="借阅人姓名/手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" clearable placeholder="请选择类型" style="width: 120px">
            <el-option label="缴纳押金" :value="1" />
            <el-option label="退还押金" :value="2" />
            <el-option label="扣除押金" :value="3" />
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
        <span>押金明细列表</span>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="borrowerName" label="借阅人" width="100" />
        <el-table-column prop="borrowerPhone" label="手机号" width="130" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getTypeColor(scope.row.type)" size="small">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="right">
          <template slot-scope="scope">
            <span :class="getAmountClass(scope.row.type)">
              {{ scope.row.type === 1 ? '+' : '-' }}¥{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeBalance" label="变更前余额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ scope.row.beforeBalance }}
          </template>
        </el-table-column>
        <el-table-column prop="afterBalance" label="变更后余额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ scope.row.afterBalance }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center">
          <template slot-scope="scope">
            {{ getPaymentMethodText(scope.row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="orderNo" label="关联订单" width="150" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
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
  </div>
</template>

<script>
import { getDepositList } from '@/api/deposit'

export default {
  name: 'Deposit',
  data() {
    return {
      loading: false,
      searchForm: {
        keyword: '',
        type: null,
        borrowerId: null
      },
      tableData: [],
      page: 1,
      size: 10,
      total: 0
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    getTypeText(type) {
      const map = { 1: '缴纳押金', 2: '退还押金', 3: '扣除押金' }
      return map[type] || '未知'
    },
    getTypeColor(type) {
      const map = { 1: 'success', 2: 'warning', 3: 'danger' }
      return map[type] || 'info'
    },
    getAmountClass(type) {
      return type === 1 ? 'text-success' : 'text-danger'
    },
    getPaymentMethodText(method) {
      const map = { 1: '现金', 2: '微信', 3: '支付宝', 4: '银行卡' }
      return map[method] || '-'
    },
    fetchData() {
      this.loading = true
      const params = {
        page: this.page,
        size: this.size,
        keyword: this.searchForm.keyword,
        type: this.searchForm.type,
        borrowerId: this.searchForm.borrowerId
      }
      getDepositList(params).then(res => {
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
        type: null,
        borrowerId: null
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
    }
  }
}
</script>

<style scoped>
.deposit-container {
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
.text-success {
  color: #67c23a;
  font-weight: bold;
}
.text-danger {
  color: #f56c6c;
  font-weight: bold;
}
</style>
