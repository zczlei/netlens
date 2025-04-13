/**
 * 广告流量分析系统配置文件
 */

// 设备类型检测
const isMobileDevice = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
    typeof navigator !== 'undefined' ? navigator.userAgent : ''
);

export const CONFIG = {
    // API 配置
    API_URL: process.env.REACT_APP_API_URL || 'http://localhost:8000/api/traffic',
    API_VERSION: 'v1',
    
    // 数据收集配置
    BASE_COLLECTION_INTERVAL: 30000, // 基础间隔30秒
    IDLE_COLLECTION_INTERVAL: 120000, // 空闲状态下收集间隔120秒
    ACTIVE_THRESHOLD: 5000, // 最近5秒内有交互则视为活跃
    MAX_EVENTS_PER_BATCH: isMobileDevice ? 50 : 100, // 移动设备降低事件数量
    
    // 功能开关
    ENABLE_MOUSE_TRACKING: true,
    ENABLE_SCROLL_TRACKING: true,
    ENABLE_CLICK_TRACKING: true,
    
    // 调试配置
    DEBUG: process.env.NODE_ENV === 'development',
    LOG_LEVEL: process.env.NODE_ENV === 'development' ? 'debug' : 'error',
    
    // 性能配置
    THROTTLE_MOUSE_EVENTS: isMobileDevice ? 200 : 100, // 移动设备增加节流时间
    DEBOUNCE_SCROLL_EVENTS: isMobileDevice ? 500 : 250, // 移动设备增加防抖时间
    
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
    
    // 节能模式
    BATTERY_SAVING_MODE: false, // 默认关闭
    DATA_COMPRESSION: true, // 启用数据压缩
    CACHE_LIFETIME: 10000, // 缓存有效期10秒
};

// 环境特定配置
if (process.env.NODE_ENV === 'development') {
    CONFIG.API_URL = 'http://localhost:8080/api/traffic';
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
    CONFIG.BASE_COLLECTION_INTERVAL = 5000; // 测试环境缩短收集间隔
}

// 如果浏览器支持，初始化电池API
if (typeof navigator !== 'undefined' && navigator.getBattery) {
    navigator.getBattery().then(battery => {
        // 低电量时启用省电模式
        CONFIG.BATTERY_SAVING_MODE = battery.level < 0.2;
        
        battery.addEventListener('levelchange', () => {
            CONFIG.BATTERY_SAVING_MODE = battery.level < 0.2;
        });
    }).catch(err => {
        console.log('电池API不可用:', err);
    });
}

// 导出配置验证函数
export const validateConfig = () => {
    const requiredFields = ['API_URL', 'BASE_COLLECTION_INTERVAL'];
    const missingFields = requiredFields.filter(field => !CONFIG[field]);
    
    if (missingFields.length > 0) {
        throw new Error(`Missing required configuration fields: ${missingFields.join(', ')}`);
    }
    
    if (CONFIG.BASE_COLLECTION_INTERVAL < 1000) {
        throw new Error('BASE_COLLECTION_INTERVAL must be at least 1000ms');
    }

    // 验证API URL格式
    try {
        new URL(CONFIG.API_URL);
    } catch (e) {
        throw new Error('Invalid API_URL format');
    }
    
    return true;
}; 