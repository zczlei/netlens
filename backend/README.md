# 广告流量分析系统 - 后端服务

本项目是广告流量分析系统的后端服务，基于Spring Boot开发，提供流量数据收集、分析和评分功能。

## 技术栈

- Java 11+
- Spring Boot 2.x
- MaxMind GeoIP2
- Lombok
- Maven/Gradle

## 项目结构

```
backend/
├── src/main/java/com/example/adtraffic/
│   ├── controller/    # REST API控制器
│   ├── service/       # 业务逻辑服务
│   ├── model/         # 数据模型
│   └── config/        # 配置类
└── src/main/resources/
    └── application.properties  # 应用配置文件
```

## 核心功能

1. **流量数据接收**
   - 接收前端采集的用户行为数据
   - 支持批量数据上报
   - IP地址和User-Agent解析

2. **多维度分析**
   - IP特征分析（30分）
   - 点击行为分析（30分）
   - 会话行为分析（25分）
   - 设备指纹分析（15分）

3. **评分系统**
   - 实时评分反馈
   - 详细的评分维度说明
   - 可配置的评分规则

## 快速开始

1. **环境要求**
   ```bash
   java -version  # 确保Java 11+
   ```

2. **配置修改**
   - 复制`application.properties.example`为`application.properties`
   - 修改数据库连接等配置

3. **启动服务**
   ```bash
   ./mvnw spring-boot:run  # Maven
   # 或
   ./gradlew bootRun      # Gradle
   ```

4. **验证安装**
   ```bash
   curl http://localhost:8080/api/health
   ```

## API文档

### 流量分析接口

POST `/api/traffic-analysis`
```json
{
  "ip": "string",
  "userAgent": "string",
  "startTime": "long",
  "clicks": "array",
  "mouseMovements": "array",
  "scrollEvents": "array",
  "sessionData": "object",
  "deviceFingerprint": "object"
}
```

响应示例：
```json
{
  "totalScore": 85,
  "details": {
    "ipScore": 25,
    "clickScore": 28,
    "sessionScore": 20,
    "deviceScore": 12
  },
  "conclusion": "真实流量"
}
```

## 配置说明

主要配置项（application.properties）：

```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/api

# 跨域配置
cors.allowed-origins=*
cors.allowed-methods=GET,POST,PUT,DELETE
cors.allowed-headers=*

# GeoIP数据库
geoip.database.path=classpath:GeoLite2-City.mmdb

# 评分规则配置
scoring.rules.ip-weight=30
scoring.rules.click-weight=30
scoring.rules.session-weight=25
scoring.rules.device-weight=15
```

## 开发指南

1. **添加新的评分维度**
   - 在`TrafficScoringService`中添加新的评分方法
   - 更新总分计算逻辑
   - 在`ScoreResult`中添加新的详情字段

2. **自定义评分规则**
   - 修改`application.properties`中的权重配置
   - 在相应的评分方法中实现具体逻辑

3. **数据持久化**
   - 实现`Repository`接口
   - 添加数据库配置
   - 更新服务层代码

## 注意事项

1. 生产环境部署前检查安全配置
2. 定期更新GeoIP数据库
3. 监控服务性能和资源使用
4. 注意数据隐私合规要求 