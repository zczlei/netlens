/**
 * 广告流量分析系统配置文件
 */

export const CONFIG = {
    // API 配置
    API_URL: process.env.REACT_APP_API_URL || 'http://localhost:8000/api/traffic',
    API_VERSION: 'v1',
    
    // 数据收集配置
    COLLECTION_INTERVAL: 30000, // 30秒
    MAX_EVENTS_PER_BATCH: 100,
    
    // 功能开关
    ENABLE_MOUSE_TRACKING: true,
    ENABLE_SCROLL_TRACKING: true,
    ENABLE_CLICK_TRACKING: true,
    
    // 调试配置
    DEBUG: process.env.NODE_ENV === 'development',
    LOG_LEVEL: process.env.NODE_ENV === 'development' ? 'debug' : 'error',
    
    // 性能配置
    THROTTLE_MOUSE_EVENTS: 100, // 节流时间（毫秒）
    DEBOUNCE_SCROLL_EVENTS: 250, // 防抖时间（毫秒）
    
    // 数据过滤
    EXCLUDED_DOMAINS: [], // 不收集数据的域名列表
    EXCLUDED_PATHS: ['/privacy', '/terms'], // 不收集数据的路径列表
    
    // 隐私设置
    ANONYMIZE_IP: true,
    COLLECT_DEVICE_INFO: true,
    
    // 重试策略
    MAX_RETRIES: 3,
    RETRY_DELAY: 1000, // 毫秒
    
    // 数据存储
    LOCAL_STORAGE_KEY: 'ad_traffic_data',
    MAX_STORAGE_SIZE: 5 * 1024 * 1024, // 5MB
};

// 环境特定配置
if (process.env.NODE_ENV === 'development') {
    CONFIG.API_URL = 'http://localhost:8000/api/traffic';
    CONFIG.DEBUG = true;
    CONFIG.LOG_LEVEL = 'debug';
} else if (process.env.NODE_ENV === 'production') {
    CONFIG.API_URL = 'https://api.yourdomain.com/api/traffic';
    CONFIG.DEBUG = false;
    CONFIG.LOG_LEVEL = 'error';
    CONFIG.ANONYMIZE_IP = true;
} else if (process.env.NODE_ENV === 'test') {
    CONFIG.API_URL = 'http://localhost:8000/api/traffic-test';
    CONFIG.DEBUG = true;
    CONFIG.LOG_LEVEL = 'debug';
    CONFIG.COLLECTION_INTERVAL = 5000; // 测试环境缩短收集间隔
}

// 导出配置验证函数
export const validateConfig = () => {
    const requiredFields = ['API_URL', 'COLLECTION_INTERVAL'];
    const missingFields = requiredFields.filter(field => !CONFIG[field]);
    
    if (missingFields.length > 0) {
        throw new Error(`Missing required configuration fields: ${missingFields.join(', ')}`);
    }
    
    if (CONFIG.COLLECTION_INTERVAL < 1000) {
        throw new Error('COLLECTION_INTERVAL must be at least 1000ms');
    }

    // 验证API URL格式
    try {
        new URL(CONFIG.API_URL);
    } catch (e) {
        throw new Error('Invalid API_URL format');
    }
    
    return true;
}; 