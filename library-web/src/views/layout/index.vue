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
        <el-popover
          placement="bottom"
          width="400"
          trigger="click"
          v-model="remindPopoverVisible"
          popper-class="remind-popover"
        >
          <div class="remind-header">
            <span class="remind-title">逾期提醒</span>
            <el-button 
              type="text" 
              size="mini" 
              @click="handleMarkAllRead"
              :disabled="unreadCount === 0"
            >
              全部已读
            </el-button>
          </div>
          <div class="remind-list" v-loading="loadingRemind">
            <div 
              v-if="unreadReminds.length === 0" 
              class="empty-remind"
            >
              <i class="el-icon-check-circle"></i>
              <p>暂无逾期提醒</p>
            </div>
            <div 
              v-for="item in unreadReminds" 
              :key="item.id"
              class="remind-item"
              :class="{ unread: item.isRead === 0 }"
              @click="handleRemindClick(item)"
            >
              <div class="remind-icon-wrapper">
                <i class="el-icon-warning remind-icon"></i>
              </div>
              <div class="remind-content">
                <p class="remind-text">{{ item.remindContent }}</p>
                <p class="remind-time">
                  {{ item.createTime }} · 第{{ item.remindDay }}次提醒
                </p>
              </div>
              <div class="remind-arrow">
                <i class="el-icon-arrow-right"></i>
              </div>
            </div>
          </div>
          <div class="remind-footer" v-if="unreadReminds.length > 0">
            <el-button type="text" size="mini" @click="goToBorrowPage">
              查看全部订单 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
          <div class="remind-bell" slot="reference" @click="loadUnreadReminds">
            <i class="el-icon-bell"></i>
            <el-badge 
              :value="unreadCount" 
              :hidden="unreadCount === 0"
              class="remind-badge"
            ></el-badge>
          </div>
        </el-popover>
        
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
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { logout } from '@/api/auth'
import { getRemindPage, markAsRead, markAllAsRead } from '@/api/remind'

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
        { path: '/remind', title: '提醒管理', icon: 'el-icon-bell' },
        { path: '/users', title: '用户管理', icon: 'el-icon-user' },
        { path: '/departments', title: '部门管理', icon: 'el-icon-office-building' },
        { path: '/employees', title: '人员管理', icon: 'el-icon-s-custom' }
      ],
      remindPopoverVisible: false,
      loadingRemind: false,
      unreadReminds: [],
      unreadCount: 0,
      remindTimer: null
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    activeMenu() {
      return this.$route.path
    }
  },
  mounted() {
    this.loadUnreadReminds()
    this.startRemindTimer()
  },
  beforeDestroy() {
    if (this.remindTimer) {
      clearInterval(this.remindTimer)
    }
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
    async loadUnreadReminds() {
      this.loadingRemind = true
      try {
        const res = await getRemindPage({
          page: 1,
          size: 10,
          isRead: 0,
          remindType: 1
        })
        if (res.code === 200) {
          this.unreadReminds = res.data.records
          this.unreadCount = res.data.total
          
          if (this.unreadCount > 0) {
            this.showRemindNotification()
          }
        }
      } catch (error) {
        console.error('加载提醒失败:', error)
      } finally {
        this.loadingRemind = false
      }
    },
    showRemindNotification() {
      if (!this.$notify) return
      
      this.$notify({
        title: '逾期提醒',
        message: `您有${this.unreadCount}条逾期未还提醒，请及时处理！`,
        type: 'warning',
        duration: 10000,
        position: 'top-right',
        onClick: () => {
          this.remindPopoverVisible = true
        }
      })
    },
    async handleRemindClick(item) {
      if (item.isRead === 0) {
        await markAsRead(item.id)
        this.unreadCount--
      }
      this.remindPopoverVisible = false
      if (this.$route.path !== '/borrow') {
        this.$router.push('/borrow').catch(err => {
          if (err.name !== 'NavigationDuplicated') {
            console.error('路由跳转失败:', err)
          }
        })
      }
    },
    async handleMarkAllRead() {
      try {
        await markAllAsRead()
        this.unreadReminds.forEach(item => {
          item.isRead = 1
        })
        this.unreadCount = 0
        this.$message.success('已全部标记为已读')
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    goToBorrowPage() {
      this.remindPopoverVisible = false
      if (this.$route.path !== '/borrow') {
        this.$router.push('/borrow').catch(err => {
          if (err.name !== 'NavigationDuplicated') {
            console.error('路由跳转失败:', err)
          }
        })
      }
    },
    startRemindTimer() {
      this.remindTimer = setInterval(() => {
        this.loadUnreadReminds()
      }, 60000)
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

.remind-bell {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  margin-right: 16px;
  transition: all 0.3s;
}

.remind-bell:hover {
  background-color: #F5F7FA;
}

.remind-bell i {
  font-size: 20px;
  color: #595959;
}

.remind-badge {
  position: absolute;
  top: 2px;
  right: 2px;
}

.remind-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #F0F0F0;
  margin-bottom: 8px;
}

.remind-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.remind-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-remind {
  padding: 40px 20px;
  text-align: center;
  color: #8C8C8C;
}

.empty-remind i {
  font-size: 48px;
  color: #5AD8A6;
  margin-bottom: 12px;
}

.empty-remind p {
  margin: 0;
}

.remind-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 4px;
}

.remind-item.unread {
  background-color: #FFF7E6;
  border-left: 3px solid #FAAD14;
}

.remind-item:hover {
  background-color: #F5F7FA;
}

.remind-item.unread:hover {
  background-color: #FFF2CC;
}

.remind-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF9C6E 0%, #FF7D00 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.remind-icon {
  font-size: 18px;
  color: #fff;
}

.remind-content {
  flex: 1;
  min-width: 0;
}

.remind-text {
  font-size: 14px;
  color: #262626;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remind-time {
  font-size: 12px;
  color: #8C8C8C;
  margin: 0;
}

.remind-arrow {
  color: #BFBFBF;
  margin-left: 8px;
}

.remind-footer {
  padding-top: 12px;
  border-top: 1px solid #F0F0F0;
  margin-top: 8px;
  text-align: right;
}
</style>
