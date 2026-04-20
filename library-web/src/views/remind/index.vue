<template>
  <div class="remind-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters" size="small">
        <el-form-item label="状态">
          <el-select v-model="filters.isRead" clearable placeholder="全部状态">
            <el-option label="未读" :value="0"></el-option>
            <el-option label="已读" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.remindType" clearable placeholder="全部类型">
            <el-option label="平台提醒" :value="1"></el-option>
            <el-option label="短信提醒" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetFilter">重置</el-button>
          <el-button type="warning" icon="el-icon-check" @click="handleMarkAllRead" :disabled="unreadCount === 0">
            全部已读
          </el-button>
          <el-button type="danger" icon="el-icon-set-up" @click="handleProcess">
            触发逾期处理
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="24" style="margin-bottom: 20px;">
      <el-col :span="8">
        <div class="stat-item blue">
          <div class="stat-icon">
            <i class="el-icon-bell"></i>
          </div>
          <div class="stat-info">
            <p class="stat-label">未读提醒</p>
            <p class="stat-value">{{ overview.unreadCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-item orange">
          <div class="stat-icon">
            <i class="el-icon-date"></i>
          </div>
          <div class="stat-info">
            <p class="stat-label">今日提醒</p>
            <p class="stat-value">{{ overview.todayCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-item red">
          <div class="stat-icon">
            <i class="el-icon-warning"></i>
          </div>
          <div class="stat-info">
            <p class="stat-label">逾期订单</p>
            <p class="stat-value">{{ overview.overdueCount || 0 }}</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card v-loading="loading">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
        <el-table-column prop="borrowerName" label="借阅人" width="100"></el-table-column>
        <el-table-column prop="borrowerPhone" label="手机号" width="120"></el-table-column>
        <el-table-column prop="bookTitle" label="图书名称" min-width="150"></el-table-column>
        <el-table-column label="提醒类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.remindType === 1 ? 'primary' : 'success'" size="small">
              {{ scope.row.remindType === 1 ? '平台提醒' : '短信提醒' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提醒天数" width="100">
          <template slot-scope="scope">
            <el-badge :value="'第' + scope.row.remindDay + '天'" :hidden="false" class="remind-day-badge">
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="remindContent" label="提醒内容" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isRead === 0 ? 'warning' : 'info'" size="small">
              {{ scope.row.isRead === 0 ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="120" fixed="right" v-if="!isReadOnly">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="handleMarkRead(scope.row)"
              :disabled="scope.row.isRead === 1"
            >
              标记已读
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;"
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
import { getRemindPage, getRemindOverview, markAsRead, markAllAsRead, processOverdueOrders } from '@/api/remind'

export default {
  name: 'Remind',
  data() {
    return {
      loading: false,
      filters: {
        isRead: null,
        remindType: null
      },
      overview: {},
      unreadCount: 0,
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      }
    }
  },
  computed: {
    isReadOnly() {
      return this.filters.isRead === 1
    }
  },
  mounted() {
    this.loadData()
    this.loadOverview()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size,
          ...this.filters
        }
        const res = await getRemindPage(params)
        if (res.code === 200) {
          this.tableData = res.data.records
          this.pagination.total = res.data.total
        }
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    async loadOverview() {
      try {
        const res = await getRemindOverview()
        if (res.code === 200) {
          this.overview = res.data
          this.unreadCount = res.data.unreadCount || 0
        }
      } catch (error) {
        console.error('加载概览数据失败:', error)
      }
    },
    async handleMarkRead(row) {
      try {
        await markAsRead(row.id)
        row.isRead = 1
        this.$message.success('标记已读成功')
        this.loadOverview()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async handleMarkAllRead() {
      try {
        await this.$confirm('确定将所有未读提醒标记为已读吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await markAllAsRead()
        this.$message.success('全部标记已读成功')
        this.loadData()
        this.loadOverview()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },
    async handleProcess() {
      try {
        await this.$confirm('确定触发逾期订单处理吗？这将为逾期订单生成提醒并发送短信。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await processOverdueOrders()
        this.$message.success('逾期处理完成')
        this.loadData()
        this.loadOverview()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('处理失败')
        }
      }
    },
    resetFilter() {
      this.filters = {
        isRead: null,
        remindType: null
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    }
  }
}
</script>

<style scoped>
.remind-container {
  padding-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.stat-item {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-item.blue .stat-icon {
  background: linear-gradient(135deg, #5B8FF9 0%, #6F9EFD 100%);
}

.stat-item.orange .stat-icon {
  background: linear-gradient(135deg, #F6BD16 0%, #F8C93A 100%);
}

.stat-item.red .stat-icon {
  background: linear-gradient(135deg, #E8684A 0%, #ED8569 100%);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon i {
  font-size: 28px;
  color: #fff;
}

.stat-label {
  font-size: 14px;
  color: #8C8C8C;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  line-height: 1;
  margin: 0;
}

.remind-day-badge {
  background-color: #FF7D00;
  color: #fff;
}
</style>