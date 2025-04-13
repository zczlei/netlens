<template>
  <div class="score-display">
    <el-card class="score-card">
      <template #header>
        <div class="card-header">
          <h2>广告流量评分结果</h2>
        </div>
      </template>
      
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
              </el-descriptions-item>
              <el-descriptions-item label="数据中心/代理检查">
                得分：{{ details.proxyScore }}/15
              </el-descriptions-item>
              <el-descriptions-item label="IP行为模式">
                得分：{{ details.ipBehaviorScore }}/10
              </el-descriptions-item>
              <el-descriptions-item label="地理位置一致性">
                得分：{{ details.geoLocationScore }}/5
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item title="点击和展示行为分析" name="2">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="点击速度和频率">
                得分：{{ details.clickSpeedScore }}/10
              </el-descriptions-item>
              <el-descriptions-item label="点击模式随机性">
                得分：{{ details.clickPatternScore }}/10
              </el-descriptions-item>
              <el-descriptions-item label="点击来源路径">
                得分：{{ details.clickPathScore }}/5
              </el-descriptions-item>
              <el-descriptions-item label="展示互动">
                得分：{{ details.displayInteractionScore }}/5
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item title="用户会话和行为分析" name="3">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="会话时长">
                得分：{{ details.sessionDurationScore }}/10
              </el-descriptions-item>
              <el-descriptions-item label="用户互动深度">
                得分：{{ details.userInteractionScore }}/10
              </el-descriptions-item>
              <el-descriptions-item label="转化行为一致性">
                得分：{{ details.conversionScore }}/5
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item title="设备和浏览器指纹分析" name="4">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="设备指纹重复性">
                得分：{{ details.deviceFingerprintScore }}/5
              </el-descriptions-item>
              <el-descriptions-item label="浏览器行为">
                得分：{{ details.browserBehaviorScore }}/5
              </el-descriptions-item>
              <el-descriptions-item label="User-Agent可信度">
                得分：{{ details.userAgentScore }}/5
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
      }
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
</style> 