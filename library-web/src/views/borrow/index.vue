<template>
  <div class="borrow-container">
    <el-card class="borrow-card">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="借阅人员" name="borrower">
          <borrower-page />
        </el-tab-pane>
        <el-tab-pane label="押金明细" name="deposit">
          <deposit-page />
        </el-tab-pane>
        <el-tab-pane label="借阅订单" name="order">
          <borrow-order-page />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import BorrowerPage from '@/views/borrower/index.vue'
import DepositPage from '@/views/deposit/index.vue'
import BorrowOrderPage from '@/views/borrowOrder/index.vue'

export default {
  name: 'Borrow',
  components: {
    BorrowerPage,
    DepositPage,
    BorrowOrderPage
  },
  data() {
    return {
      activeTab: 'borrower'
    }
  }
}
</script>

<style scoped>
.borrow-container {
  padding: 20px;
}
.borrow-card {
  min-height: calc(100vh - 104px);
}
.el-tabs__content {
  padding: 0;
}
</style>
