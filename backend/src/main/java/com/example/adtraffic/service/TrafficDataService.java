package com.example.adtraffic.service;

import com.example.adtraffic.model.TrafficData;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TrafficDataService {

    // 使用内存队列存储最近的流量数据
    private final ConcurrentLinkedQueue<TrafficData> recentTrafficData = new ConcurrentLinkedQueue<>();
    private static final int MAX_QUEUE_SIZE = 100;

    /**
     * 保存流量数据到队列中
     */
    public TrafficData saveTrafficData(TrafficData data) {
        // 确保队列不会过大
        if (recentTrafficData.size() >= MAX_QUEUE_SIZE) {
            recentTrafficData.poll(); // 移除最旧的数据
        }
        
        // 更新IP地址（如果为空）
        if (data.getIp() == null || data.getIp().isEmpty()) {
            data.setIp("127.0.0.1"); // 使用本地IP代替
        }
        
        recentTrafficData.add(data);
        log.info("保存流量数据成功，当前队列大小: {}", recentTrafficData.size());
        return data;
    }

    /**
     * 获取最新的流量数据
     */
    public TrafficData getLatestTrafficData() {
        return recentTrafficData.isEmpty() ? null : recentTrafficData.peek();
    }

    /**
     * 定期清理旧数据
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupOldData() {
        int initialSize = recentTrafficData.size();
        if (initialSize > MAX_QUEUE_SIZE / 2) {
            List<TrafficData> tempList = new ArrayList<>(recentTrafficData);
            recentTrafficData.clear();
            
            // 只保留最新的一半数据
            int keepSize = initialSize / 2;
            for (int i = initialSize - keepSize; i < initialSize; i++) {
                recentTrafficData.add(tempList.get(i));
            }
            
            log.info("清理旧数据完成，从 {} 条减少到 {} 条", initialSize, recentTrafficData.size());
        }
    }
} 