import { CONFIG, validateConfig } from './config';

describe('广告流量分析系统配置测试', () => {
    const originalEnv = process.env;
    
    beforeEach(() => {
        // 备份原始环境变量
        process.env = { ...originalEnv };
    });

    afterEach(() => {
        // 恢复原始环境变量
        process.env = originalEnv;
    });

    test('基本配置验证', () => {
        expect(CONFIG).toBeDefined();
        expect(CONFIG.API_URL).toBeDefined();
        expect(CONFIG.COLLECTION_INTERVAL).toBeDefined();
    });

    test('开发环境配置', () => {
        process.env.NODE_ENV = 'development';
        // 重新加载配置
        jest.resetModules();
        const { CONFIG } = require('./config');
        
        expect(CONFIG.API_URL).toBe('http://localhost:8000/api/traffic');
        expect(CONFIG.DEBUG).toBe(true);
        expect(CONFIG.LOG_LEVEL).toBe('debug');
    });

    test('生产环境配置', () => {
        process.env.NODE_ENV = 'production';
        // 重新加载配置
        jest.resetModules();
        const { CONFIG } = require('./config');
        
        expect(CONFIG.API_URL).toBe('https://api.yourdomain.com/api/traffic');
        expect(CONFIG.DEBUG).toBe(false);
        expect(CONFIG.LOG_LEVEL).toBe('error');
        expect(CONFIG.ANONYMIZE_IP).toBe(true);
    });

    test('测试环境配置', () => {
        process.env.NODE_ENV = 'test';
        // 重新加载配置
        jest.resetModules();
        const { CONFIG } = require('./config');
        
        expect(CONFIG.API_URL).toBe('http://localhost:8000/api/traffic-test');
        expect(CONFIG.DEBUG).toBe(true);
        expect(CONFIG.LOG_LEVEL).toBe('debug');
        expect(CONFIG.COLLECTION_INTERVAL).toBe(5000);
    });

    test('环境变量覆盖默认API URL', () => {
        process.env.REACT_APP_API_URL = 'http://custom-api.test/traffic';
        // 重新加载配置
        jest.resetModules();
        const { CONFIG } = require('./config');
        
        expect(CONFIG.API_URL).toBe('http://custom-api.test/traffic');
    });

    describe('配置验证函数测试', () => {
        test('有效配置应通过验证', () => {
            expect(validateConfig()).toBe(true);
        });

        test('无效的收集间隔应抛出错误', () => {
            CONFIG.COLLECTION_INTERVAL = 500;
            expect(() => validateConfig()).toThrow('COLLECTION_INTERVAL must be at least 1000ms');
        });

        test('无效的API URL应抛出错误', () => {
            CONFIG.API_URL = 'invalid-url';
            expect(() => validateConfig()).toThrow('Invalid API_URL format');
        });
    });

    test('性能配置验证', () => {
        expect(CONFIG.THROTTLE_MOUSE_EVENTS).toBeGreaterThan(0);
        expect(CONFIG.DEBOUNCE_SCROLL_EVENTS).toBeGreaterThan(0);
    });

    test('存储配置验证', () => {
        expect(CONFIG.LOCAL_STORAGE_KEY).toBe('ad_traffic_data');
        expect(CONFIG.MAX_STORAGE_SIZE).toBeGreaterThan(0);
    });
}); 