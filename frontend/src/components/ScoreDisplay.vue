<template>
  <div class="score-display">
    <el-card class="score-card">
      <template #header>
        <div class="card-header">
          <h2>广告流量评分结果</h2>
          <el-button type="primary" size="small" @click="fetchLatestScore" :loading="loading">
            刷新数据
          </el-button>
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
        <div class="traffic-source-info">
          <h3>流量来源信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="IP地址">{{ rawData.ip || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="用户代理">{{ rawData.userAgent || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="设备指纹">{{ rawData.deviceFingerprint || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="会话开始时间">{{ new Date(rawData.startTime).toLocaleString() }}</el-descriptions-item>
            <el-descriptions-item label="流量收集时间">{{ new Date().toLocaleString() }}</el-descriptions-item>
            <el-descriptions-item v-if="rawData.source && rawData.source.page" label="来源页面">{{ rawData.source.page }}</el-descriptions-item>
            <el-descriptions-item v-if="rawData.source && rawData.source.url" label="页面URL">{{ rawData.source.url }}</el-descriptions-item>
            <el-descriptions-item v-if="rawData.source && rawData.source.referrer" label="引荐来源">{{ rawData.source.referrer || '直接访问' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="score-section">
          <el-progress
            type="dashboard"
            :percentage="score"
            :color="scoreColor"
            :status="scoreStatus">
          </el-progress>
          <h3 class="score-label">{{ scoreLabel }}</h3>
        </div>

        <div class="details-section">
          <h3>详细评分</h3>
          <el-collapse v-model="activeNames">
            <el-collapse-item title="IP地址和网络特征分析" name="1">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="恶意IP检查">
                  {{ details.ipMalicious ? '已检测到恶意IP' : '未检测到恶意IP' }}
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>IP: {{ rawData.ip || '未提供' }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统会检查IP是否存在于已知的恶意IP数据库中。当前IP {{ details.ipMalicious ? '被' : '未被' }}识别为恶意IP。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="数据中心/代理检查">
                  得分：{{ details.proxyScore }}/15
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>IP: {{ rawData.ip || '未提供' }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统会检查IP是否来自数据中心或代理服务器。一般的家庭/移动网络IP会得到较高分数，而数据中心IP会得到较低分数。</p>
                      <p>当前IP得分为{{ details.proxyScore }}/15。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="IP行为模式">
                  得分：{{ details.ipBehaviorScore }}/10
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>IP: {{ rawData.ip || '未提供' }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析该IP的历史行为模式，包括点击频率、访问时间分布等。</p>
                      <p>当前IP的行为模式得分为{{ details.ipBehaviorScore }}/10。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="地理位置一致性">
                  得分：{{ details.geoLocationScore }}/5
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>IP: {{ rawData.ip || '未提供' }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统检查IP地理位置是否与用户行为一致，例如时区、语言设置等。</p>
                      <p>当前地理位置一致性得分为{{ details.geoLocationScore }}/5。</p>
                    </div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>

            <el-collapse-item title="点击和展示行为分析" name="2">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="点击速度和频率">
                  得分：{{ details.clickSpeedScore }}/10
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>点击事件数: {{ rawData.clicks.length }}</pre>
                      <template v-if="rawData.clicks.length > 0">
                        <p>点击事件详情:</p>
                        <pre>{{ JSON.stringify(rawData.clicks, null, 2) }}</pre>
                      </template>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析点击的时间间隔和频率。人类用户的点击通常有一定的随机性和合理的时间间隔。</p>
                      <p>当前点击速度和频率得分为{{ details.clickSpeedScore }}/10。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="点击模式随机性">
                  得分：{{ details.clickPatternScore }}/10
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>点击事件数: {{ rawData.clicks.length }}</pre>
                      <template v-if="rawData.clicks.length > 0">
                        <p>点击事件详情:</p>
                        <pre>{{ JSON.stringify(rawData.clicks, null, 2) }}</pre>
                      </template>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析点击位置的分布和随机性。人类用户的点击通常有一定的随机分布。</p>
                      <p>当前点击模式随机性得分为{{ details.clickPatternScore }}/10。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="点击来源路径">
                  得分：{{ details.clickPathScore }}/5
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>点击事件数: {{ rawData.clicks.length }}</pre>
                      <template v-if="rawData.clicks.length > 0">
                        <p>点击事件详情:</p>
                        <pre>{{ JSON.stringify(rawData.clicks, null, 2) }}</pre>
                      </template>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析点击前的鼠标移动路径。真实用户通常有自然的移动路径。</p>
                      <p>当前点击来源路径得分为{{ details.clickPathScore }}/5。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="展示互动">
                  得分：{{ details.displayInteractionScore }}/5
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>鼠标移动事件数: {{ rawData.mouseMovements.length }}</pre>
                      <template v-if="rawData.mouseMovements.length > 0">
                        <p>鼠标移动事件详情:</p>
                        <pre>{{ JSON.stringify(rawData.mouseMovements.slice(0, 3), null, 2) }}{{ rawData.mouseMovements.length > 3 ? '...' : '' }}</pre>
                      </template>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析用户与广告展示的互动，如悬停、移动等行为。</p>
                      <p>当前展示互动得分为{{ details.displayInteractionScore }}/5。</p>
                    </div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>

            <el-collapse-item title="用户会话和行为分析" name="3">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="会话时长">
                  得分：{{ details.sessionDurationScore }}/10
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>会话开始时间: {{ new Date(rawData.startTime).toLocaleString() }}</pre>
                      <pre>会话数据: {{ JSON.stringify(rawData.sessionData, null, 2) }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析用户会话的持续时间。真实用户通常会有一定的浏览时间。</p>
                      <p>当前会话时长得分为{{ details.sessionDurationScore }}/10。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="用户互动深度">
                  得分：{{ details.userInteractionScore }}/10
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>点击事件数: {{ rawData.clicks.length }}</pre>
                      <pre>鼠标移动事件数: {{ rawData.mouseMovements.length }}</pre>
                      <pre>滚动事件数: {{ rawData.scrollEvents.length }}</pre>
                      <pre>互动总数: {{ rawData.sessionData.interactions || 0 }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析用户与页面的互动深度，包括点击、滚动、表单填写等。</p>
                      <p>当前用户互动深度得分为{{ details.userInteractionScore }}/10。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="转化行为一致性">
                  得分：{{ details.conversionScore }}/5
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>点击事件数: {{ rawData.clicks.length }}</pre>
                      <template v-if="rawData.clicks.length > 0">
                        <p>点击事件详情:</p>
                        <pre>{{ JSON.stringify(rawData.clicks, null, 2) }}</pre>
                      </template>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析用户的转化行为是否与整体浏览行为一致。</p>
                      <p>当前转化行为一致性得分为{{ details.conversionScore }}/5。</p>
                    </div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>

            <el-collapse-item title="设备和浏览器指纹分析" name="4">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="设备指纹重复性">
                  得分：{{ details.deviceFingerprintScore }}/5
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>设备指纹: {{ rawData.deviceFingerprint }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统检查设备指纹是否在短时间内多次出现。频繁重复的设备指纹可能表示欺诈流量。</p>
                      <p>当前设备指纹重复性得分为{{ details.deviceFingerprintScore }}/5。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="浏览器行为">
                  得分：{{ details.browserBehaviorScore }}/5
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>User-Agent: {{ rawData.userAgent }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统分析浏览器行为的一致性，包括Cookie支持、JavaScript执行、屏幕尺寸等。</p>
                      <p>当前浏览器行为得分为{{ details.browserBehaviorScore }}/5。</p>
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="User-Agent可信度">
                  得分：{{ details.userAgentScore }}/5
                  <div class="score-detail">
                    <div class="raw-data">
                      <h4>原始数据</h4>
                      <pre>User-Agent: {{ rawData.userAgent }}</pre>
                    </div>
                    <div class="scoring-process">
                      <h4>打分过程</h4>
                      <p>系统检查User-Agent字符串的真实性和一致性。</p>
                      <p>当前User-Agent可信度得分为{{ details.userAgentScore }}/5。</p>
                    </div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
          </el-collapse>
        </div>

        <div class="conclusion-section">
          <el-alert
            :title="conclusionTitle"
            :type="conclusionType"
            :description="conclusionDescription"
            show-icon>
          </el-alert>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ScoreDisplay',
  data() {
    return {
      activeNames: ['1'],
      score: 0,
      details: {
        ipMalicious: false,
        proxyScore: 0,
        ipBehaviorScore: 0,
        geoLocationScore: 0,
        clickSpeedScore: 0,
        clickPatternScore: 0,
        clickPathScore: 0,
        displayInteractionScore: 0,
        sessionDurationScore: 0,
        userInteractionScore: 0,
        conversionScore: 0,
        deviceFingerprintScore: 0,
        browserBehaviorScore: 0,
        userAgentScore: 0
      },
      rawData: {
        ip: '',
        userAgent: '',
        startTime: 0,
        clicks: [],
        mouseMovements: [],
        scrollEvents: [],
        sessionData: {},
        deviceFingerprint: ''
      },
      loading: false,
      error: null,
      timer: null
    }
  },
  created() {
    // 组件创建时获取最新的评分数据
    this.fetchLatestScore();
    
    // 每30秒自动刷新数据
    this.timer = setInterval(() => {
      this.fetchLatestScore();
    }, 30000);
  },
  beforeUnmount() {
    // 组件销毁前清除定时器
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  computed: {
    scoreColor() {
      if (this.score >= 80) return '#67C23A'
      if (this.score >= 50) return '#E6A23C'
      return '#F56C6C'
    },
    scoreStatus() {
      if (this.score >= 80) return 'success'
      if (this.score >= 50) return 'warning'
      return 'exception'
    },
    scoreLabel() {
      if (this.score >= 80) return '真实流量'
      if (this.score >= 50) return '可疑流量'
      return '假流量'
    },
    conclusionTitle() {
      return `流量评估结果：${this.scoreLabel}`
    },
    conclusionType() {
      if (this.score >= 80) return 'success'
      if (this.score >= 50) return 'warning'
      return 'error'
    },
    conclusionDescription() {
      if (this.score >= 80) {
        return '该流量在大多数指标上表现正常，可以认定为真实有效流量。'
      }
      if (this.score >= 50) {
        return '该流量存在部分异常特征，建议进行进一步审查。'
      }
      return '该流量在多个关键指标上表现异常，极可能是欺诈性流量。'
    }
  },
  methods: {
    updateScore(scoreData) {
      this.score = scoreData.totalScore
      this.details = scoreData.details
    },
    async fetchLatestScore() {
      this.loading = true;
      this.error = null;
      
      try {
        // 首先尝试获取最近的流量数据
        const latestDataResponse = await fetch('/api/traffic/latest', {
          method: 'GET',
          headers: {
            'Accept': 'application/json'
          }
        });
        
        let trafficData;
        
        if (latestDataResponse.ok) {
          trafficData = await latestDataResponse.json();
          console.log('获取到最近收集的流量数据', trafficData);
        } else {
          // 如果获取不到最近数据，则使用默认测试数据
          trafficData = {
            ip: '',
            userAgent: navigator.userAgent,
            startTime: new Date().getTime(),
            clicks: [],
            mouseMovements: [],
            scrollEvents: [],
            sessionData: {
              duration: 0,
              interactions: 0
            },
            deviceFingerprint: 'test-fingerprint'
          };
          console.log('使用默认测试数据', trafficData);
        }

        // 保存原始数据
        this.rawData = JSON.parse(JSON.stringify(trafficData));
        
        // 发送请求到后端API进行分析
        const response = await fetch('/api/traffic-analysis', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(trafficData)
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        this.updateScore(result);
        console.log('获取到最新评分数据', result);
      } catch (error) {
        this.error = `获取数据失败: ${error.message}`;
        console.error('获取评分数据失败:', error);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.score-display {
  max-width: 800px;
  margin: 20px auto;
}

.score-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.traffic-source-info {
  margin-bottom: 25px;
  padding: 15px;
  background-color: #f0f8ff;
  border-radius: 6px;
  border: 1px solid #e0f0ff;
}

.traffic-source-info h3 {
  color: #1989fa;
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  border-bottom: 1px solid #e0f0ff;
  padding-bottom: 10px;
}

.score-section {
  text-align: center;
  padding: 20px 0;
}

.score-label {
  margin-top: 15px;
  font-size: 24px;
  color: #303133;
}

.details-section {
  margin-top: 20px;
}

.conclusion-section {
  margin-top: 20px;
}

.loading-container {
  padding: 20px;
}

.score-detail {
  margin-top: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  font-size: 14px;
}

.raw-data {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f0f9ff;
  border-left: 3px solid #409EFF;
  border-radius: 3px;
}

.raw-data h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #409EFF;
  font-size: 15px;
}

.raw-data pre {
  margin: 5px 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
  font-size: 13px;
}

.scoring-process {
  padding: 10px;
  background-color: #f0fff0;
  border-left: 3px solid #67C23A;
  border-radius: 3px;
}

.scoring-process h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #67C23A;
  font-size: 15px;
}

.scoring-process p {
  margin: 5px 0;
  line-height: 1.5;
}
</style> 