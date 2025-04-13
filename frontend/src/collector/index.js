import DataCollector from '../components/DataCollector';
import { CONFIG, validateConfig } from './config';

// 验证配置
try {
    validateConfig();
} catch (error) {
    console.error('配置验证失败:', error.message);
    throw error;
}

// 创建全局实例
window.AdTrafficCollector = new DataCollector();

// 自动开始收集数据
const collector = window.AdTrafficCollector;

// 重试函数
async function sendDataWithRetry() {
    let retries = 0;
    while (retries < CONFIG.MAX_RETRIES) {
        try {
            const result = await collector.sendDataToServer();
            if (CONFIG.DEBUG) {
                console.log('流量分析结果:', result);
            }
            return result;
        } catch (error) {
            retries++;
            if (CONFIG.DEBUG) {
                console.error(`发送数据失败 (尝试 ${retries}/${CONFIG.MAX_RETRIES}):`, error);
            }
            if (retries < CONFIG.MAX_RETRIES) {
                await new Promise(resolve => setTimeout(resolve, CONFIG.RETRY_DELAY));
            } else {
                throw error;
            }
        }
    }
}

// 定期发送数据到服务器
setInterval(async () => {
    try {
        await sendDataWithRetry();
    } catch (error) {
        console.error('流量分析失败:', error);
    }
}, CONFIG.COLLECTION_INTERVAL); 