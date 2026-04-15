<template>
  <div class="borrow-container">
    <el-card class="borrow-card">
      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
        <el-tab-pane label="借阅人员" name="borrower">
          <borrower-page />
        </el-tab-pane>
        <el-tab-pane label="押金明细" name="deposit">
          <deposit-page />
        </el-tab-pane>
        <el-tab-pane label="借阅订单" name="order">
          <borrow-order-page :highlight-order-id="highlightOrderId" :key="componentKey" />
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
      activeTab: 'borrower',
      highlightOrderId: null,
      componentKey: 0
    }
  },
  created() {
    this.checkRouteQuery()
  },
  watch: {
    '$route.query': {
      handler(newQuery, oldQuery) {
        const newOrderId = newQuery.orderId
        const oldOrderId = oldQuery ? oldQuery.orderId : null
        
        if (newOrderId !== oldOrderId) {
          this.checkRouteQuery()
        }
      },
      deep: true
    }
  },
  methods: {
    checkRouteQuery() {
      const tab = this.$route.query.tab
      const orderId = this.$route.query.orderId
      
      if (tab) {
        this.activeTab = tab
      }
      
      if (orderId) {
        this.highlightOrderId = parseInt(orderId)
        this.componentKey++
      } else {
        this.highlightOrderId = null
      }
    },
    handleTabClick(tab) {
      if (tab.name !== 'order') {
        this.highlightOrderId = null
      }
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
