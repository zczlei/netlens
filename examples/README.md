# 广告流量分析系统示例

本目录包含了广告流量分析系统的示例和演示文件。

## 文件结构

- `demo.html`: 演示页面，展示了各种广告单元的布局和交互效果
- `static/`: 静态资源目录（图片、样式等）

## 使用方法

1. 确保后端服务已启动（默认端口8080）
2. 直接在浏览器中打开 `demo.html` 文件
3. 通过浏览器开发者工具的控制台查看数据收集日志（需要启用DEBUG模式）

## 演示功能

- 广告展示追踪
- 点击事件收集
- 鼠标悬停时间统计
- 用户行为数据批量上报
- 实时数据分析反馈

## 配置说明

演示页面中的配置参数可以在`demo.html`文件中的`CONFIG`对象中修改：

```javascript
const CONFIG = {
    API_URL: 'http://localhost:8080/api/traffic',
    COLLECTION_INTERVAL: 30000,
    MAX_EVENTS_PER_BATCH: 100,
    DEBUG: true,
    THROTTLE_MOUSE_EVENTS: 100,
    DEBOUNCE_SCROLL_EVENTS: 250
};
```

## 注意事项

1. 示例代码仅用于演示目的
2. 生产环境中请关闭DEBUG模式
3. 根据实际需求调整数据收集频率
4. 注意遵守数据隐私相关法规 