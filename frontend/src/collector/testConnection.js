import { CONFIG, validateConfig } from './config';

async function testConnection() {
    console.log('开始测试配置和API连接...');
    
    try {
        // 1. 验证配置
        console.log('1. 验证配置...');
        validateConfig();
        console.log('✅ 配置验证通过');
        console.log('当前配置:', {
            API_URL: CONFIG.API_URL,
            环境: process.env.NODE_ENV,
            调试模式: CONFIG.DEBUG,
            日志级别: CONFIG.LOG_LEVEL,
            数据收集间隔: CONFIG.COLLECTION_INTERVAL
        });

        // 2. 测试API连接
        console.log('\n2. 测试API连接...');
        const response = await fetch(CONFIG.API_URL, {
            method: 'OPTIONS',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            console.log('✅ API连接成功');
            console.log('服务器响应状态:', response.status);
            // 尝试获取服务器支持的方法
            const allowedMethods = response.headers.get('Allow');
            if (allowedMethods) {
                console.log('支持的HTTP方法:', allowedMethods);
            }
        } else {
            throw new Error(`API连接失败: ${response.status} ${response.statusText}`);
        }

        // 3. 测试数据发送
        console.log('\n3. 测试数据发送...');
        const testData = {
            type: 'test',
            timestamp: new Date().toISOString(),
            data: {
                test: true,
                environment: process.env.NODE_ENV
            }
        };

        const sendResponse = await fetch(CONFIG.API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(testData)
        });

        if (sendResponse.ok) {
            const result = await sendResponse.json();
            console.log('✅ 测试数据发送成功');
            console.log('服务器响应:', result);
        } else {
            throw new Error(`数据发送失败: ${sendResponse.status} ${sendResponse.statusText}`);
        }

    } catch (error) {
        console.error('❌ 测试失败:', error.message);
        if (CONFIG.DEBUG) {
            console.error('详细错误信息:', error);
        }
    }
}

// 如果直接运行此文件则执行测试
if (require.main === module) {
    testConnection();
}

export default testConnection; 