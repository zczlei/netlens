import { CONFIG } from '../collector/config';

class DataCollector {
    constructor() {
        this.data = {
            ip: '',
            userAgent: navigator.userAgent,
            startTime: new Date().getTime(),
            clicks: [],
            mouseMovements: [],
            scrollEvents: [],
            sessionData: {
                duration: 0,
                interactions: 0,
                conversions: 0
            },
            deviceFingerprint: CONFIG.COLLECT_DEVICE_INFO ? this.generateDeviceFingerprint() : null,
            conversions: [],
            adMetrics: {
                totalImpressions: 0,
                totalClicks: 0,
                totalHoverTime: 0,
                clickThroughRate: 0,
                averageHoverTime: 0
            }
        };
        
        // 记录用户活动时间
        this.lastUserActivity = Date.now();
        this.collectionTimer = null;
        
        // 初始化本地存储
        this.initializeStorage();
        this.initializeCollectors();
        
        // 启动自适应数据收集
        this.scheduleNextCollection();
    }

    initializeStorage() {
        try {
            const storedData = localStorage.getItem(CONFIG.LOCAL_STORAGE_KEY);
            if (storedData) {
                const parsedData = JSON.parse(storedData);
                // 合并存储的数据
                this.data = { ...this.data, ...parsedData };
            }
        } catch (error) {
            if (CONFIG.DEBUG) {
                console.error('初始化本地存储失败:', error);
            }
        }
    }

    saveToStorage() {
        try {
            const dataString = JSON.stringify(this.data);
            if (dataString.length <= CONFIG.MAX_STORAGE_SIZE) {
                localStorage.setItem(CONFIG.LOCAL_STORAGE_KEY, dataString);
            } else if (CONFIG.DEBUG) {
                console.warn('数据大小超过限制，跳过本地存储');
            }
        } catch (error) {
            if (CONFIG.DEBUG) {
                console.error('保存到本地存储失败:', error);
            }
        }
    }

    initializeCollectors() {
        if (CONFIG.ENABLE_CLICK_TRACKING) {
            this.initializeClickTracking();
        }
        
        if (CONFIG.ENABLE_MOUSE_TRACKING) {
            this.initializeMouseTracking();
        }
        
        if (CONFIG.ENABLE_SCROLL_TRACKING) {
            this.initializeScrollTracking();
        }

        // 记录用户活动
        ['mousemove', 'click', 'keydown', 'scroll', 'touchstart'].forEach(eventType => {
            document.addEventListener(eventType, () => this.recordUserActivity(), { passive: true });
        });

        // 更新会话时长
        setInterval(() => {
            this.data.sessionData.duration = 
                (new Date().getTime() - this.data.startTime) / 1000;
            this.saveToStorage();
        }, 1000);
    }
    
    // 记录用户活动时间
    recordUserActivity() {
        this.lastUserActivity = Date.now();
    }
    
    // 判断用户是否活跃
    isUserActive() {
        return (Date.now() - this.lastUserActivity) < CONFIG.ACTIVE_THRESHOLD;
    }
    
    // 安排下一次数据收集
    scheduleNextCollection() {
        if (this.collectionTimer) {
            clearTimeout(this.collectionTimer);
        }
        
        const interval = this.isUserActive() ? 
            CONFIG.BASE_COLLECTION_INTERVAL : 
            CONFIG.IDLE_COLLECTION_INTERVAL;
            
        if (CONFIG.DEBUG) {
            console.log(`安排下一次数据收集，间隔: ${interval}ms，用户状态: ${this.isUserActive() ? '活跃' : '空闲'}`);
        }
            
        this.collectionTimer = setTimeout(() => {
            this.sendDataToServer();
            this.scheduleNextCollection();
        }, interval);
    }

    initializeClickTracking() {
        document.addEventListener('click', (e) => {
            // 检查是否在排除路径中
            if (this.isExcludedPath(window.location.pathname)) {
                return;
            }

            this.data.clicks.push({
                timestamp: new Date().getTime(),
                x: e.clientX,
                y: e.clientY,
                target: e.target.tagName,
                path: window.location.pathname
            });
            
            this.data.sessionData.interactions++;
            
            if (CONFIG.DEBUG) {
                console.log('点击已记录:', this.data.clicks[this.data.clicks.length - 1]);
            }
        });
    }

    initializeMouseTracking() {
        let lastMove = 0;
        document.addEventListener('mousemove', (e) => {
            const now = new Date().getTime();
            if (now - lastMove > CONFIG.THROTTLE_MOUSE_EVENTS) {
                if (!this.isExcludedPath(window.location.pathname)) {
                    this.data.mouseMovements.push({
                        timestamp: now,
                        x: e.clientX,
                        y: e.clientY,
                        path: window.location.pathname
                    });
                }
                lastMove = now;
            }
        });
    }

    initializeScrollTracking() {
        let scrollTimeout;
        document.addEventListener('scroll', () => {
            if (scrollTimeout) {
                clearTimeout(scrollTimeout);
            }
            
            scrollTimeout = setTimeout(() => {
                if (!this.isExcludedPath(window.location.pathname)) {
                    this.data.scrollEvents.push({
                        timestamp: new Date().getTime(),
                        scrollY: window.scrollY,
                        path: window.location.pathname
                    });
                    this.data.sessionData.interactions++;
                }
            }, CONFIG.DEBOUNCE_SCROLL_EVENTS);
        });
    }

    isExcludedPath(path) {
        return CONFIG.EXCLUDED_PATHS.some(excludedPath => 
            path.startsWith(excludedPath));
    }

    generateDeviceFingerprint() {
        if (!CONFIG.COLLECT_DEVICE_INFO) {
            return null;
        }

        // 简单的设备指纹生成
        const screenPrint = `${screen.width}x${screen.height}x${screen.colorDepth}`;
        const timezoneOffset = new Date().getTimezoneOffset();
        const plugins = Array.from(navigator.plugins).map(p => p.name).join(',');
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        ctx.textBaseline = "top";
        ctx.font = "14px 'Arial'";
        ctx.textBaseline = "alphabetic";
        ctx.fillStyle = "#f60";
        ctx.fillRect(125,1,62,20);
        ctx.fillStyle = "#069";
        ctx.fillText("Hello, world!", 2, 15);
        ctx.fillStyle = "rgba(102, 204, 0, 0.7)";
        ctx.fillText("Hello, world!", 4, 17);
        
        return btoa(
            screenPrint + 
            timezoneOffset + 
            plugins +
            canvas.toDataURL()
        );
    }

    async getIPInfo() {
        if (this.data.ip) {
            return;
        }

        try {
            const response = await fetch('https://api.ipify.org?format=json');
            const data = await response.json();
            this.data.ip = CONFIG.ANONYMIZE_IP ? 
                this.anonymizeIP(data.ip) : data.ip;
        } catch (error) {
            if (CONFIG.DEBUG) {
                console.error('获取IP失败:', error);
            }
        }
    }

    anonymizeIP(ip) {
        // 将IP地址的最后一个八位字节替换为0
        return ip.replace(/\d+$/, '0');
    }

    updateAdMetrics() {
        const impressions = this.data.conversions.filter(c => c.type === 'impression').length;
        const clicks = this.data.conversions.filter(c => c.type === 'click').length;
        const hovers = this.data.conversions.filter(c => c.type === 'hover');
        
        const totalHoverTime = hovers.reduce((sum, h) => sum + (h.duration || 0), 0);
        
        this.data.adMetrics = {
            totalImpressions: impressions,
            totalClicks: clicks,
            totalHoverTime: totalHoverTime,
            clickThroughRate: impressions > 0 ? (clicks / impressions) * 100 : 0,
            averageHoverTime: hovers.length > 0 ? totalHoverTime / hovers.length : 0
        };
    }

    getCollectedData() {
        // 清理过期数据
        this.cleanupOldData();
        // 更新广告指标
        this.updateAdMetrics();
        
        return {
            ...this.data,
            sessionData: {
                ...this.data.sessionData,
                duration: (new Date().getTime() - this.data.startTime) / 1000
            }
        };
    }

    // 获取压缩后的数据
    getCompressedData() {
        // 根据重要性对数据排序
        const prioritizedData = {
            // 高优先级：基本信息和直接转化相关
            essential: {
                ip: this.data.ip,
                userAgent: this.data.userAgent,
                deviceFingerprint: this.data.deviceFingerprint,
                startTime: this.data.startTime,
                sessionData: {
                    duration: (new Date().getTime() - this.data.startTime) / 1000,
                    interactions: this.data.sessionData.interactions,
                    conversions: this.data.sessionData.conversions
                },
                clicks: this.data.clicks.filter(click => 
                    click.timestamp > Date.now() - 3600000) // 最近1小时的点击
            },
            
            // 低优先级：辅助分析数据
            nonEssential: {
                mouseMovements: this.getReducedMouseMovements(),
                scrollEvents: this.data.scrollEvents.slice(-10), // 最近10次滚动事件
                conversions: this.data.conversions,
                adMetrics: this.data.adMetrics
            }
        };
        
        // 省电模式下只发送高优先级数据
        return CONFIG.BATTERY_SAVING_MODE ? 
            prioritizedData.essential : 
            {...prioritizedData.essential, ...prioritizedData.nonEssential};
    }
    
    // 鼠标移动数据降采样
    getReducedMouseMovements() {
        // 如果数据量小，直接返回
        if (this.data.mouseMovements.length < 10) {
            return this.data.mouseMovements.slice();
        }
        
        // 否则进行降采样，只取约10%的数据点
        const samplingRate = Math.max(1, Math.floor(this.data.mouseMovements.length / 10));
        const sampledData = [];
        
        for (let i = 0; i < this.data.mouseMovements.length; i += samplingRate) {
            sampledData.push(this.data.mouseMovements[i]);
        }
        
        return sampledData;
    }

    cleanupOldData() {
        const now = new Date().getTime();
        const maxAge = 24 * 60 * 60 * 1000; // 24小时

        this.data.clicks = this.data.clicks.filter(click => 
            now - click.timestamp < maxAge);
        this.data.mouseMovements = this.data.mouseMovements.filter(movement => 
            now - movement.timestamp < maxAge);
        this.data.scrollEvents = this.data.scrollEvents.filter(event => 
            now - event.timestamp < maxAge);
        this.data.conversions = this.data.conversions.filter(conversion => 
            now - conversion.timestamp < maxAge);
    }

    async sendDataToServer() {
        await this.getIPInfo();
        
        // 使用压缩数据
        const dataToSend = CONFIG.DATA_COMPRESSION ? 
            this.getCompressedData() : this.getCollectedData();
        
        if (CONFIG.DEBUG) {
            console.log('准备发送数据到服务器:', 
                `原始数据大小: ${JSON.stringify(this.data).length}字节, ` +
                `发送数据大小: ${JSON.stringify(dataToSend).length}字节`);
        }
        
        try {
            const response = await fetch(CONFIG.API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(dataToSend)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // 清理已发送的数据
            this.data.clicks = [];
            this.data.mouseMovements = [];
            this.data.scrollEvents = [];
            
            if (CONFIG.DEBUG) {
                console.log('数据发送成功');
            }
        } catch (error) {
            if (CONFIG.DEBUG) {
                console.error('发送数据失败:', error);
            }
            
            // 保存到本地存储以便稍后重试
            this.saveToStorage();
        }
    }
}

export default DataCollector; 