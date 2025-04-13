<template>
  <div class="traffic-list">
    <el-card class="traffic-card">
      <template #header>
        <div class="card-header">
          <h2>流量评分记录列表</h2>
          <div class="header-controls">
            <el-select v-model="filterType" placeholder="筛选类型" size="small" @change="fetchTrafficRecords">
              <el-option label="全部" value=""></el-option>
              <el-option label="真实流量" value="真实流量"></el-option>
              <el-option label="可疑流量" value="可疑流量"></el-option>
              <el-option label="假流量" value="假流量"></el-option>
            </el-select>
            <el-input-number v-model="recordLimit" :min="5" :max="100" size="small" @change="fetchTrafficRecords"></el-input-number>
            <el-button type="primary" size="small" @click="fetchTrafficRecords" :loading="loading">
              刷新数据
            </el-button>
          </div>
        </div>
      </template>
      
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        style="margin-bottom: 15px;">
      </el-alert>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton animated :rows="10" />
      </div>
      
      <div v-else>
        <!-- 统计数据 -->
        <div class="statistics-container">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" :body-style="{padding: '10px'}">
                <div class="statistic-item">
                  <div class="statistic-title">真实流量</div>
                  <div class="statistic-value success">{{ statistics['真实流量'] || 0 }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" :body-style="{padding: '10px'}">
                <div class="statistic-item">
                  <div class="statistic-title">可疑流量</div>
                  <div class="statistic-value warning">{{ statistics['可疑流量'] || 0 }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" :body-style="{padding: '10px'}">
                <div class="statistic-item">
                  <div class="statistic-title">假流量</div>
                  <div class="statistic-value danger">{{ statistics['假流量'] || 0 }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 记录表格 -->
        <el-table
          :data="records"
          style="width: 100%; margin-top: 20px;"
          border
          stripe
          :default-sort="{ prop: 'createdAt', order: 'descending' }"
          v-loading="tableLoading">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="ip" label="IP地址" width="140"></el-table-column>
          <el-table-column label="访问时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalScore" label="总分" width="80">
            <template #default="scope">
              <el-tag :type="getScoreTagType(scope.row.totalScore)" effect="light">
                {{ scope.row.totalScore }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="评分详情" width="240">
            <template #default="scope">
              <div class="score-details">
                <el-tooltip content="IP和网络特征分析 (30分)">
                  <div class="score-item">IP: {{ scope.row.ipScore }}/30</div>
                </el-tooltip>
                <el-tooltip content="点击和展示行为分析 (30分)">
                  <div class="score-item">点击: {{ scope.row.clickScore }}/30</div>
                </el-tooltip>
                <el-tooltip content="用户会话和行为分析 (25分)">
                  <div class="score-item">会话: {{ scope.row.sessionScore }}/25</div>
                </el-tooltip>
                <el-tooltip content="设备和浏览器指纹分析 (15分)">
                  <div class="score-item">设备: {{ scope.row.deviceScore }}/15</div>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="conclusion" label="结论" width="100">
            <template #default="scope">
              <el-tag :type="getConclusionTagType(scope.row.conclusion)">
                {{ scope.row.conclusion }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button type="text" @click="viewDetails(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog title="流量记录详情" v-model="detailDialogVisible" width="70%">
      <div v-if="selectedRecord">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="IP地址">{{ selectedRecord.ip }}</el-descriptions-item>
          <el-descriptions-item label="用户代理">{{ selectedRecord.userAgent }}</el-descriptions-item>
          <el-descriptions-item label="访问时间">{{ formatDate(selectedRecord.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="总分">
            <el-tag :type="getScoreTagType(selectedRecord.totalScore)">{{ selectedRecord.totalScore }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="结论">
            <el-tag :type="getConclusionTagType(selectedRecord.conclusion)">{{ selectedRecord.conclusion }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="details-section">
          <h3>评分详情</h3>
          <div class="score-breakdown">
            <el-progress 
              type="dashboard" 
              :percentage="Math.round(selectedRecord.ipScore / 30 * 100)" 
              :color="getProgressColor(selectedRecord.ipScore / 30 * 100)">
              <template #default>
                <div class="progress-content">
                  <span class="progress-value">{{ selectedRecord.ipScore }}</span>
                  <span class="progress-label">IP分析</span>
                </div>
              </template>
            </el-progress>
            
            <el-progress 
              type="dashboard" 
              :percentage="Math.round(selectedRecord.clickScore / 30 * 100)" 
              :color="getProgressColor(selectedRecord.clickScore / 30 * 100)">
              <template #default>
                <div class="progress-content">
                  <span class="progress-value">{{ selectedRecord.clickScore }}</span>
                  <span class="progress-label">点击行为</span>
                </div>
              </template>
            </el-progress>
            
            <el-progress 
              type="dashboard" 
              :percentage="Math.round(selectedRecord.sessionScore / 25 * 100)" 
              :color="getProgressColor(selectedRecord.sessionScore / 25 * 100)">
              <template #default>
                <div class="progress-content">
                  <span class="progress-value">{{ selectedRecord.sessionScore }}</span>
                  <span class="progress-label">会话特征</span>
                </div>
              </template>
            </el-progress>
            
            <el-progress 
              type="dashboard" 
              :percentage="Math.round(selectedRecord.deviceScore / 15 * 100)" 
              :color="getProgressColor(selectedRecord.deviceScore / 15 * 100)">
              <template #default>
                <div class="progress-content">
                  <span class="progress-value">{{ selectedRecord.deviceScore }}</span>
                  <span class="progress-label">设备指纹</span>
                </div>
              </template>
            </el-progress>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TrafficList',
  data() {
    return {
      records: [],
      statistics: {},
      loading: false,
      tableLoading: false,
      error: null,
      filterType: '',
      recordLimit: 20,
      detailDialogVisible: false,
      selectedRecord: null,
      refreshTimer: null
    }
  },
  created() {
    this.fetchTrafficRecords();
    
    // 定期自动刷新
    this.refreshTimer = setInterval(() => {
      this.fetchTrafficRecords(true);
    }, 60000); // 每分钟刷新一次
  },
  beforeUnmount() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
  },
  methods: {
    async fetchTrafficRecords(silent = false) {
      if (!silent) {
        this.loading = true;
      } else {
        this.tableLoading = true;
      }
      
      this.error = null;
      let retryCount = 0;
      const maxRetries = 3;
      
      const tryFetch = async () => {
        try {
          const response = await fetch(`/api/traffic-records?conclusion=${this.filterType}&limit=${this.recordLimit}`, {
            method: 'GET',
            headers: {
              'Accept': 'application/json'
            }
          });
          
          if (!response.ok) {
            if (response.status === 404) {
              throw new Error('API端点不存在，请检查后端服务是否正确配置');
            } else if (response.status === 500) {
              throw new Error('服务器内部错误，请稍后再试');
            } else {
              throw new Error(`HTTP错误! 状态码: ${response.status}`);
            }
          }
          
          const data = await response.json();
          this.records = data.records || [];
          this.statistics = data.statistics || {};
        } catch (e) {
          console.error('获取流量记录失败:', e);
          if (retryCount < maxRetries) {
            retryCount++;
            console.log(`正在重试 (${retryCount}/${maxRetries})...`);
            return new Promise(resolve => setTimeout(() => resolve(tryFetch()), 1000));
          } else {
            this.error = `获取流量记录失败: ${e.message}. 请检查后端服务是否启动，或稍后再试。`;
            this.records = [];
            this.statistics = {};
          }
        }
      };
      
      await tryFetch();
      
      this.loading = false;
      this.tableLoading = false;
    },
    getScoreTagType(score) {
      if (score >= 80) return 'success';
      if (score >= 50) return 'warning';
      return 'danger';
    },
    getConclusionTagType(conclusion) {
      if (conclusion === '真实流量') return 'success';
      if (conclusion === '可疑流量') return 'warning';
      return 'danger';
    },
    getProgressColor(percentage) {
      if (percentage >= 80) return '#67C23A';
      if (percentage >= 50) return '#E6A23C';
      return '#F56C6C';
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString();
    },
    viewDetails(record) {
      this.selectedRecord = record;
      this.detailDialogVisible = true;
    }
  }
}
</script>

<style scoped>
.traffic-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.loading-container {
  padding: 20px 0;
}

.statistics-container {
  margin-bottom: 20px;
}

.statistic-item {
  text-align: center;
  padding: 10px;
}

.statistic-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.statistic-value {
  font-size: 24px;
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

.score-details {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.score-item {
  font-size: 12px;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.details-section {
  margin-top: 20px;
}

.score-breakdown {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-top: 20px;
}

.progress-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.progress-value {
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 4px;
}

.progress-label {
  font-size: 12px;
}
</style> 