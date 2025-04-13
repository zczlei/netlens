<template>
  <div class="home-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-menu
          :default-active="activeIndex"
          class="nav-menu"
          router>
          <el-menu-item index="/">
            <el-icon><i class="el-icon-s-home"></i></el-icon>
            <span>首页概览</span>
          </el-menu-item>
          <el-menu-item index="/score">
            <el-icon><i class="el-icon-data-analysis"></i></el-icon>
            <span>单个流量分析</span>
          </el-menu-item>
          <el-menu-item index="/traffic-list">
            <el-icon><i class="el-icon-s-data"></i></el-icon>
            <span>流量记录列表</span>
          </el-menu-item>
        </el-menu>
        
        <el-card class="system-info" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <h3>系统信息</h3>
            </div>
          </template>
          <div class="info-item">
            <strong>系统状态:</strong> <el-tag type="success">运行中</el-tag>
          </div>
          <div class="info-item">
            <strong>后端服务:</strong> <span>http://localhost:8080</span>
          </div>
          <div class="info-item">
            <strong>前端服务:</strong> <span>http://localhost:8081</span>
          </div>
          <div class="info-item">
            <strong>数据收集周期:</strong> <span>30秒</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <div class="dashboard">
          <div v-if="activeIndex === '/'">
            <h2>博色广告流量分析系统</h2>
            <p>欢迎使用博色广告流量分析系统，本系统帮助您识别和过滤虚假流量，提高广告投放效率。</p>
            
            <el-card class="summary-card">
              <template #header>
                <div class="card-header">
                  <h3>流量分析概览</h3>
                </div>
              </template>
              <div class="summary-content" v-loading="loading">
                <div v-if="statistics" class="statistics-container">
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <el-card shadow="hover" :body-style="{padding: '15px'}">
                        <div class="statistic-item">
                          <div class="statistic-title">真实流量</div>
                          <div class="statistic-value success">{{ statistics['真实流量'] || 0 }}</div>
                        </div>
                      </el-card>
                    </el-col>
                    <el-col :span="8">
                      <el-card shadow="hover" :body-style="{padding: '15px'}">
                        <div class="statistic-item">
                          <div class="statistic-title">可疑流量</div>
                          <div class="statistic-value warning">{{ statistics['可疑流量'] || 0 }}</div>
                        </div>
                      </el-card>
                    </el-col>
                    <el-col :span="8">
                      <el-card shadow="hover" :body-style="{padding: '15px'}">
                        <div class="statistic-item">
                          <div class="statistic-title">假流量</div>
                          <div class="statistic-value danger">{{ statistics['假流量'] || 0 }}</div>
                        </div>
                      </el-card>
                    </el-col>
                  </el-row>
                </div>
                
                <div class="action-btns" style="margin-top: 20px;">
                  <el-button type="primary" @click="goToPage('/score')">进行单个流量分析</el-button>
                  <el-button type="success" @click="goToPage('/traffic-list')">查看所有流量记录</el-button>
                </div>
              </div>
            </el-card>
          </div>
          
          <router-view v-else></router-view>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  data() {
    return {
      activeIndex: '/',
      statistics: null,
      loading: false,
      error: null
    }
  },
  created() {
    this.activeIndex = this.$route.path;
    this.fetchStatistics();
  },
  watch: {
    $route(to) {
      this.activeIndex = to.path;
    }
  },
  methods: {
    async fetchStatistics() {
      this.loading = true;
      try {
        const response = await fetch('/api/traffic-records?limit=0', {
          method: 'GET',
          headers: {
            'Accept': 'application/json'
          }
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        this.statistics = data.statistics || {};
      } catch (e) {
        console.error('获取统计数据失败:', e);
        this.error = `获取统计数据失败: ${e.message}`;
      } finally {
        this.loading = false;
      }
    },
    goToPage(path) {
      this.$router.push(path);
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

.nav-menu {
  border-radius: 4px;
}

.dashboard {
  padding: 0 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  margin-bottom: 10px;
  font-size: 14px;
}

.system-info {
  font-size: 14px;
}

.summary-card {
  margin-top: 20px;
}

.summary-content {
  min-height: 300px;
}

.statistic-item {
  text-align: center;
  padding: 15px;
}

.statistic-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.statistic-value {
  font-size: 28px;
  font-weight: bold;
}

.statistic-value.success {
  color: #67C23A;
}

.statistic-value.warning {
  color: #E6A23C;
}

.statistic-value.danger {
  color: #F56C6C;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}
</style> 