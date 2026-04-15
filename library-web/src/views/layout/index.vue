<template>
  <el-container class="layout-container">
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
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-tooltip content="消息提醒" placement="bottom">
            <el-button type="text" class="notification-btn" @click="showNotificationPanel">
              <i class="el-icon-bell"></i>
            </el-button>
          </el-tooltip>
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
    
    <el-main class="main-content">
      <router-view />
    </el-main>
    
    <el-dialog
      title="消息提醒"
      :visible.sync="notificationDialogVisible"
      width="500px"
      custom-class="notification-dialog"
    >
      <div class="notification-header">
        <el-button type="text" @click="handleMarkAllRead" :disabled="unreadCount === 0">
          全部已读
        </el-button>
      </div>
      
      <div class="notification-list" v-loading="notificationLoading">
        <div v-if="notifications.length === 0" class="empty-notification">
          <i class="el-icon-message"></i>
          <p>暂无消息</p>
        </div>
        
        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          class="notification-item"
          :class="{ unread: notification.isRead === 0 }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon">
            <i :class="getNotificationIcon(notification.notificationType)"></i>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-text">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
          </div>
          <el-tag 
            v-if="notification.isRead === 0" 
            type="danger" 
            size="mini"
            effect="dark"
          >未读</el-tag>
        </div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="notificationDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToBorrowOrder">查看借阅订单</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { logout } from '@/api/auth'
import { getUnreadNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'

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
      notificationDialogVisible: false,
      notifications: [],
      unreadCount: 0,
      notificationLoading: false,
      refreshTimer: null
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    activeMenu() {
      return this.$route.path
    }
  },
  mounted() {
    this.fetchUnreadCount()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    this.stopAutoRefresh()
  },
  methods: {
    async fetchUnreadCount() {
      try {
        const res = await getUnreadCount()
        if (res.code === 200) {
          this.unreadCount = res.data
          if (this.unreadCount > 0) {
            this.showNotificationTip()
          }
        }
      } catch (error) {
        console.error('获取未读消息数量失败:', error)
      }
    },
    
    async fetchNotifications() {
      this.notificationLoading = true
      try {
        const res = await getUnreadNotifications()
        if (res.code === 200) {
          this.notifications = res.data || []
        }
      } catch (error) {
        console.error('获取消息列表失败:', error)
      } finally {
        this.notificationLoading = false
      }
    },
    
    showNotificationPanel() {
      this.notificationDialogVisible = true
      this.fetchNotifications()
    },
    
    async handleNotificationClick(notification) {
      if (notification.isRead === 0) {
        try {
          await markAsRead(notification.id)
          notification.isRead = 1
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }
      
      if (notification.orderId) {
        this.notificationDialogVisible = false
        const currentPath = this.$route.path
        const query = { 
          tab: 'order',
          orderId: notification.orderId 
        }
        
        if (currentPath === '/borrow') {
          this.$router.replace({ path: '/borrow', query })
        } else {
          this.$router.push({ path: '/borrow', query })
        }
      }
    },
    
    async handleMarkAllRead() {
      try {
        await markAllAsRead()
        this.notifications.forEach(n => n.isRead = 1)
        this.unreadCount = 0
        this.$message.success('已全部标记为已读')
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    
    goToBorrowOrder() {
      this.notificationDialogVisible = false
      this.$router.push('/borrow')
    },
    
    showNotificationTip() {
      this.$notify({
        title: '新消息提醒',
        message: `您有${this.unreadCount}条未读消息，请及时处理`,
        type: 'warning',
        duration: 5000,
        position: 'top-right',
        onClick: () => {
          this.showNotificationPanel()
        }
      })
    },
    
    getNotificationIcon(type) {
      const icons = {
        1: 'el-icon-time',
        2: 'el-icon-warning',
        3: 'el-icon-money'
      }
      return icons[type] || 'el-icon-bell'
    },
    
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return date.toLocaleDateString()
    },
    
    startAutoRefresh() {
      this.refreshTimer = setInterval(() => {
        this.fetchUnreadCount()
      }, 60000)
    },
    
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },
    
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
  gap: 16px;
}

.notification-badge {
  line-height: 1;
}

.notification-btn {
  padding: 8px;
  font-size: 20px;
  color: #595959;
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

.notification-dialog :deep(.el-dialog__body) {
  padding: 0 20px 20px;
}

.notification-header {
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 10px;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-notification {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.empty-notification i {
  font-size: 48px;
  margin-bottom: 16px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 8px;
}

.notification-item:hover {
  background-color: #F5F7FA;
}

.notification-item.unread {
  background-color: #ECF5FF;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5B8FF9 0%, #6F9EFD 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.notification-icon i {
  color: #fff;
  font-size: 18px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 13px;
  color: #595959;
  line-height: 1.5;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
