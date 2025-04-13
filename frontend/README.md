# 广告流量分析系统 - 前端项目

本项目是广告流量分析系统的前端部分，提供数据采集SDK和可视化界面。

## 技术栈

- React 18
- TypeScript
- Ant Design
- ECharts
- Axios

## 项目结构

```
frontend/
├── src/
│   ├── collector/       # 数据采集SDK
│   │   ├── index.js     # SDK入口
│   │   └── config.js    # 配置文件
│   ├── components/      # React组件
│   ├── pages/          # 页面组件
│   ├── services/       # API服务
│   └── utils/          # 工具函数
├── public/             # 静态资源
└── package.json        # 项目配置
```

## 核心功能

1. **数据采集SDK**
   - 用户行为追踪
   - 设备信息收集
   - 批量数据上报
   - 防抖和节流优化

2. **可视化界面**
   - 实时流量监控
   - 数据分析报表
   - 评分结果展示
   - 异常流量告警

## 快速开始

1. **安装依赖**
   ```bash
   npm install
   # 或
   yarn install
   ```

2. **开发环境**
   ```bash
   npm start
   # 或
   yarn start
   ```

3. **生产构建**
   ```bash
   npm run build
   # 或
   yarn build
   ```

## SDK使用指南

1. **基础集成**
   ```html
   <script src="/path/to/collector.js"></script>
   <script>
     window.AdTrafficCollector.init({
       apiUrl: 'http://your-api.com',
       debug: true
     });
   </script>
   ```

2. **配置参数**
   ```javascript
   {
     apiUrl: string,              // API地址
     collectionInterval: number,  // 数据收集间隔（毫秒）
     maxEventsPerBatch: number,  // 每批次最大事件数
     debug: boolean,             // 调试模式
     throttleMouseEvents: number // 鼠标事件节流时间
   }
   ```

3. **手动触发**
   ```javascript
   // 手动上报数据
   window.AdTrafficCollector.flush();
   
   // 添加自定义事件
   window.AdTrafficCollector.addEvent({
     type: 'custom',
     data: { /* 自定义数据 */ }
   });
   ```

## 组件开发

1. **新增组件**
   ```bash
   # 创建组件目录
   mkdir src/components/NewComponent
   
   # 创建组件文件
   touch src/components/NewComponent/index.tsx
   touch src/components/NewComponent/style.scss
   ```

2. **组件示例**
   ```typescript
   import React from 'react';
   import './style.scss';
   
   interface Props {
     data: any;
     onAction: () => void;
   }
   
   const NewComponent: React.FC<Props> = ({ data, onAction }) => {
     return (
       <div className="new-component">
         {/* 组件内容 */}
       </div>
     );
   };
   
   export default NewComponent;
   ```

## 开发指南

1. **代码规范**
   - 使用ESLint和Prettier
   - 遵循TypeScript类型定义
   - 组件文档和注释完整

2. **状态管理**
   - 使用React Context
   - 合理划分状态范围
   - 避免状态提升过深

3. **性能优化**
   - 组件懒加载
   - 合理使用useMemo和useCallback
   - 图片和资源优化

## 部署说明

1. **环境配置**
   - 创建`.env`文件
   - 设置API地址和其他环境变量

2. **构建优化**
   - 启用代码分割
   - 配置CDN地址
   - 压缩静态资源

3. **部署检查**
   - 验证API连接
   - 检查跨域配置
   - 测试数据采集功能

## 注意事项

1. 生产环境关闭调试模式
2. 合理配置数据采集频率
3. 注意用户隐私保护
4. 定期清理历史数据
5. 监控错误和性能指标

## 收集的数据

- IP 地址
- 用户代理信息
- 点击事件
- 鼠标移动轨迹
- 页面滚动事件
- 会话数据（持续时间、交互次数）
- 设备指纹

## 注意事项

1. 确保后端 API 地址配置正确
2. 考虑数据收集的频率，避免过于频繁的请求
3. 在生产环境中禁用调试模式
4. 遵守相关的数据隐私法规 