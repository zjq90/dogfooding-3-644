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
          <borrow-order-page ref="orderPage" />
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
  },
  watch: {
    '$route.query': {
      immediate: true,
      handler(query) {
        if (query.orderId) {
          this.activeTab = 'order'
          // 等待子组件渲染完成后，调用子组件方法高亮订单
          this.$nextTick(() => {
            if (this.$refs.orderPage && this.$refs.orderPage.highlightOrder) {
              this.$refs.orderPage.highlightOrder(parseInt(query.orderId))
            }
          })
        } else if (query.tab) {
          this.activeTab = query.tab
        }
      }
    }
  },
  methods: {
    handleTabClick(tab) {
      // 切换标签时，清除URL参数
      if (this.$route.query.orderId || this.$route.query.tab) {
        this.$router.replace({ path: '/borrow' })
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
