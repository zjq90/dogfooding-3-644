<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <i class="el-icon-reading"></i>
          <span class="logo-text">图书借阅管理系统</span>
        </div>
      </div>
      
      <div class="header-center">
        <el-menu
          :default-active="activeMenu"
          class="nav-menu"
          mode="horizontal"
          background-color="transparent"
          text-color="#595959"
          active-text-color="#5B8FF9"
          router
        >
          <el-menu-item 
            v-for="item in menuList" 
            :key="item.path" 
            :index="item.path"
          >
            <i :class="item.icon"></i>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <div class="header-right">
        <!-- 通知图标 -->
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-popover
            placement="bottom"
            width="350"
            trigger="click"
            popper-class="notification-popover"
          >
            <div class="notification-header">
              <span class="notification-title">通知消息</span>
              <el-button v-if="unreadCount > 0" type="text" size="mini" @click="handleMarkAllRead">
                全部已读
              </el-button>
            </div>
            <div class="notification-list">
              <div v-if="notificationList.length === 0" class="notification-empty">
                <i class="el-icon-bell"></i>
                <p>暂无未读通知</p>
              </div>
              <div
                v-for="item in notificationList"
                :key="item.id"
                class="notification-item"
                :class="{ 'unread': item.status === 0 }"
                @click="handleNotificationClick(item)"
              >
                <div class="notification-item-title">{{ item.title }}</div>
                <div class="notification-item-content">{{ item.content }}</div>
                <div class="notification-item-time">{{ formatTime(item.createTime) }}</div>
              </div>
            </div>
            <div class="notification-footer">
              <el-button type="text" size="small" @click="goToNotifications">
                查看全部
              </el-button>
            </div>
            <el-button slot="reference" type="text" class="notification-btn">
              <i class="el-icon-bell"></i>
            </el-button>
          </el-popover>
        </el-badge>
        
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar"></el-avatar>
            <span class="username">{{ userInfo.realName || userInfo.username }}</span>
            <i class="el-icon-arrow-down"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile">
              <i class="el-icon-user"></i> 个人中心
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <i class="el-icon-lock"></i> 修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <i class="el-icon-switch-button"></i> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
    
    <!-- 通知弹框 -->
    <el-dialog
      :visible.sync="notificationDialogVisible"
      title="归还提醒"
      width="500px"
      :close-on-click-modal="false"
      custom-class="reminder-dialog"
    >
      <div v-if="currentNotification" class="reminder-dialog-content">
        <div class="reminder-icon">
          <i class="el-icon-warning-outline"></i>
        </div>
        <h3 class="reminder-title">{{ currentNotification.title }}</h3>
        <p class="reminder-text">{{ currentNotification.content }}</p>
        <div class="reminder-actions">
          <el-button type="primary" @click="goToBorrowOrder">
            去处理
          </el-button>
          <el-button @click="notificationDialogVisible = false">
            稍后处理
          </el-button>
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { logout } from '@/api/auth'
import { getUnreadReturnReminders, countUnread, markAsRead, markAllAsRead } from '@/api/notification'

export default {
  name: 'Layout',
  data() {
    return {
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      menuList: [
        { path: '/dashboard', title: '数据概览', icon: 'el-icon-s-data' },
        { path: '/books', title: '图书管理', icon: 'el-icon-reading' },
        { path: '/categories', title: '分类管理', icon: 'el-icon-folder-opened' },
        { path: '/borrow', title: '借阅管理', icon: 'el-icon-document' },
        { path: '/users', title: '用户管理', icon: 'el-icon-user' },
        { path: '/departments', title: '部门管理', icon: 'el-icon-office-building' },
        { path: '/employees', title: '人员管理', icon: 'el-icon-s-custom' }
      ],
      notificationList: [],
      unreadCount: 0,
      notificationDialogVisible: false,
      currentNotification: null,
      notificationTimer: null
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    activeMenu() {
      return this.$route.path
    }
  },
  mounted() {
    this.fetchUnreadNotifications()
    this.startNotificationTimer()
  },
  beforeDestroy() {
    this.stopNotificationTimer()
  },
  methods: {
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$message.info('个人中心功能开发中')
          break
        case 'password':
          this.$message.info('修改密码功能开发中')
          break
        case 'logout':
          this.handleLogout()
          break
      }
    },
    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await logout()
        this.$store.dispatch('logout')
        this.$message.success('退出成功')
        this.$router.push('/login')
      } catch (error) {
        // 用户取消
      }
    },
    async fetchUnreadNotifications() {
      try {
        const res = await getUnreadReturnReminders()
        if (res.code === 200) {
          this.notificationList = res.data || []
        }
        
        const countRes = await countUnread()
        if (countRes.code === 200) {
          this.unreadCount = countRes.data.returnReminderUnread || 0
        }
        
        // 如果有未读通知，显示弹框
        if (this.notificationList.length > 0 && !this.notificationDialogVisible) {
          this.showNotificationDialog(this.notificationList[0])
        }
      } catch (error) {
        console.error('获取通知失败:', error)
      }
    },
    startNotificationTimer() {
      // 每30秒检查一次新通知
      this.notificationTimer = setInterval(() => {
        this.fetchUnreadNotifications()
      }, 30000)
    },
    stopNotificationTimer() {
      if (this.notificationTimer) {
        clearInterval(this.notificationTimer)
        this.notificationTimer = null
      }
    },
    showNotificationDialog(notification) {
      this.currentNotification = notification
      this.notificationDialogVisible = true
    },
    async handleNotificationClick(item) {
      if (item.status === 0) {
        try {
          await markAsRead(item.id)
          item.status = 1
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }
      
      if (item.relatedOrderId) {
        this.$router.push({
          path: '/borrow',
          query: { orderId: item.relatedOrderId }
        }).catch(err => {
          if (err.name !== 'NavigationDuplicated') {
            throw err
          }
        })
      }
    },
    async handleMarkAllRead() {
      try {
        await markAllAsRead()
        this.$message.success('全部标记已读')
        this.notificationList.forEach(item => {
          item.status = 1
        })
        this.unreadCount = 0
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    goToNotifications() {
      this.$message.info('通知列表功能开发中')
    },
    goToBorrowOrder() {
      this.notificationDialogVisible = false
      if (this.currentNotification && this.currentNotification.relatedOrderId) {
        // 使用对象形式跳转，避免重复导航报错
        this.$router.push({
          path: '/borrow',
          query: { orderId: this.currentNotification.relatedOrderId }
        }).catch(err => {
          // 忽略重复导航错误
          if (err.name !== 'NavigationDuplicated') {
            throw err
          }
        })
      } else {
        this.$router.push('/borrow').catch(() => {})
      }
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        return date.toLocaleDateString()
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: #F5F7FA;
}

.header {
  height: 64px !important;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo i {
  font-size: 32px;
  color: #5B8FF9;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  letter-spacing: 1px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item) {
  font-size: 15px;
  height: 64px;
  line-height: 64px;
  padding: 0 24px;
  margin: 0 4px;
  border-bottom: none !important;
  transition: all 0.3s;
}

.nav-menu :deep(.el-menu-item:hover) {
  background-color: rgba(91, 143, 249, 0.05) !important;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(91, 143, 249, 0.1) !important;
  font-weight: 500;
}

.nav-menu :deep(.el-menu-item i) {
  margin-right: 6px;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  cursor: pointer;
}

.notification-badge :deep(.el-badge__content) {
  background-color: #F56C6C;
}

.notification-btn {
  font-size: 20px;
  color: #595959;
  padding: 8px;
}

.notification-btn:hover {
  color: #5B8FF9;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #F5F7FA;
}

.username {
  font-size: 14px;
  color: #595959;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  margin-top: 64px;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #EBEEF5;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-empty {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.notification-empty i {
  font-size: 48px;
  margin-bottom: 12px;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #F5F7FA;
}

.notification-item.unread {
  background-color: rgba(91, 143, 249, 0.05);
}

.notification-item.unread .notification-item-title {
  color: #5B8FF9;
  font-weight: 500;
}

.notification-item-title {
  font-size: 14px;
  color: #262626;
  margin-bottom: 6px;
  line-height: 1.4;
}

.notification-item-content {
  font-size: 12px;
  color: #8C8C8C;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-item-time {
  font-size: 12px;
  color: #BFBFBF;
  margin-top: 6px;
}

.notification-footer {
  text-align: center;
  padding: 12px;
  border-top: 1px solid #EBEEF5;
}

.reminder-dialog-content {
  text-align: center;
  padding: 20px;
}

.reminder-icon {
  font-size: 64px;
  color: #E6A23C;
  margin-bottom: 20px;
}

.reminder-title {
  font-size: 18px;
  color: #262626;
  margin-bottom: 16px;
}

.reminder-text {
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
  margin-bottom: 24px;
}

.reminder-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
