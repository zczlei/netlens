package com.example.adtraffic.service;

import com.example.adtraffic.model.TrafficData;
import com.example.adtraffic.model.ScoreResult;
import com.example.adtraffic.model.TrafficScoreRecord;
import com.example.adtraffic.repository.TrafficScoreRepository;
import com.maxmind.geoip2.DatabaseReader;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.net.InetAddress;

@Service
@Slf4j
public class TrafficScoringService {
    
    private final DatabaseReader geoIpReader;
    private final Set<String> maliciousIps;
    private final TrafficScoreRepository scoreRepository;
    private final ObjectMapper objectMapper;
    
    public TrafficScoringService(TrafficScoreRepository scoreRepository, ObjectMapper objectMapper) {
        this.scoreRepository = scoreRepository;
        this.objectMapper = objectMapper;
        // 初始化GeoIP数据库和恶意IP列表
        this.geoIpReader = null; // 需要实际初始化
        this.maliciousIps = new HashSet<>();
    }
    
    @Transactional
    public ScoreResult analyzeTraffic(TrafficData data) {
        ScoreResult result = new ScoreResult();
        Map<String, Object> details = new HashMap<>();
        
        // 1. IP地址和网络特征分析 (30分)
        int ipScore = analyzeIpFeatures(data, details);
        
        // 如果是恶意IP，直接返回0分
        if (details.containsKey("ipMalicious") && (boolean)details.get("ipMalicious")) {
            result.setTotalScore(0);
            result.setDetails(details);
            saveScoreRecord(data, result, 0, 0, 0, 0);
            return result;
        }
        
        // 2. 点击和展示行为分析 (30分)
        int clickScore = analyzeClickBehavior(data, details);
        
        // 3. 用户会话和行为分析 (25分)
        int sessionScore = analyzeSessionBehavior(data, details);
        
        // 4. 设备和浏览器指纹分析 (15分)
        int deviceScore = analyzeDeviceFingerprint(data, details);
        
        // 计算总分
        int totalScore = ipScore + clickScore + sessionScore + deviceScore;
        result.setTotalScore(totalScore);
        result.setDetails(details);
        
        // 保存评分记录
        saveScoreRecord(data, result, ipScore, clickScore, sessionScore, deviceScore);
        
        return result;
    }
    
    private void saveScoreRecord(TrafficData data, ScoreResult result, 
                               int ipScore, int clickScore, 
                               int sessionScore, int deviceScore) {
        try {
            TrafficScoreRecord record = new TrafficScoreRecord();
            record.setIp(data.getIp());
            record.setUserAgent(data.getUserAgent());
            record.setTotalScore(result.getTotalScore());
            record.setIpScore(ipScore);
            record.setClickScore(clickScore);
            record.setSessionScore(sessionScore);
            record.setDeviceScore(deviceScore);
            record.setConclusion(result.getConclusion());
            record.setScoreDetails(objectMapper.writeValueAsString(result.getDetails()));
            
            scoreRepository.save(record);
            log.info("Saved traffic score record for IP: {}, Score: {}", data.getIp(), result.getTotalScore());
        } catch (Exception e) {
            log.error("Failed to save traffic score record", e);
        }
    }
    
    private int analyzeIpFeatures(TrafficData data, Map<String, Object> details) {
        int score = 0;
        
        // 1.1 检查恶意IP
        boolean isMalicious = maliciousIps.contains(data.getIp());
        details.put("ipMalicious", isMalicious);
        if (isMalicious) {
            return 0;
        }
        
        // 1.2 检查是否来自数据中心或代理
        boolean isProxy = checkIfProxy(data.getIp());
        int proxyScore = isProxy ? 0 : 15;
        details.put("proxyScore", proxyScore);
        score += proxyScore;
        
        // 1.3 IP行为模式分析
        int behaviorScore = analyzeIpBehaviorPattern(data);
        details.put("ipBehaviorScore", behaviorScore);
        score += behaviorScore;
        
        // 1.4 地理位置一致性
        int geoScore = checkGeoLocation(data);
        details.put("geoLocationScore", geoScore);
        score += geoScore;
        
        return score;
    }
    
    private int analyzeClickBehavior(TrafficData data, Map<String, Object> details) {
        int score = 0;
        
        // 2.1 点击速度和频率
        int speedScore = analyzeClickSpeed(data.getClicks());
        details.put("clickSpeedScore", speedScore);
        score += speedScore;
        
        // 2.2 点击模式随机性
        int patternScore = analyzeClickPattern(data.getClicks());
        details.put("clickPatternScore", patternScore);
        score += patternScore;
        
        // 2.3 点击来源路径
        int pathScore = analyzeClickPath(data);
        details.put("clickPathScore", pathScore);
        score += pathScore;
        
        // 2.4 展示互动
        int interactionScore = analyzeDisplayInteraction(data);
        details.put("displayInteractionScore", interactionScore);
        score += interactionScore;
        
        return score;
    }
    
    private int analyzeSessionBehavior(TrafficData data, Map<String, Object> details) {
        int score = 0;
        
        // 3.1 会话时长
        int durationScore = analyzeSessionDuration(data.getSessionData());
        details.put("sessionDurationScore", durationScore);
        score += durationScore;
        
        // 3.2 用户互动深度
        int interactionScore = analyzeUserInteraction(data);
        details.put("userInteractionScore", interactionScore);
        score += interactionScore;
        
        // 3.3 转化行为一致性
        int conversionScore = analyzeConversionBehavior(data);
        details.put("conversionScore", conversionScore);
        score += conversionScore;
        
        return score;
    }
    
    private int analyzeDeviceFingerprint(TrafficData data, Map<String, Object> details) {
        int score = 0;
        
        // 4.1 设备指纹重复性
        int fingerprintScore = checkDeviceFingerprint(data);
        details.put("deviceFingerprintScore", fingerprintScore);
        score += fingerprintScore;
        
        // 4.2 浏览器行为
        int browserScore = analyzeBrowserBehavior(data);
        details.put("browserBehaviorScore", browserScore);
        score += browserScore;
        
        // 4.3 User-Agent可信度
        int uaScore = analyzeUserAgent(data);
        details.put("userAgentScore", uaScore);
        score += uaScore;
        
        return score;
    }
    
    // 具体分析方法的实现
    private boolean checkIfProxy(String ip) {
        // 实现代理检测逻辑
        return false;
    }
    
    private int analyzeIpBehaviorPattern(TrafficData data) {
        // 分析IP行为模式
        return 10;
    }
    
    private int checkGeoLocation(TrafficData data) {
        // 检查地理位置一致性
        return 5;
    }
    
    private int analyzeClickSpeed(List<Map<String, Object>> clicks) {
        if (clicks.isEmpty()) {
            return 0;
        }
        
        // 分析点击间隔
        long lastClickTime = 0;
        int suspiciousClicks = 0;
        
        for (Map<String, Object> click : clicks) {
            long timestamp = Long.parseLong(click.get("timestamp").toString());
            if (lastClickTime > 0) {
                long interval = timestamp - lastClickTime;
                if (interval < 1000) { // 小于1秒的点击间隔
                    suspiciousClicks++;
                }
            }
            lastClickTime = timestamp;
        }
        
        // 根据可疑点击比例评分
        double suspiciousRatio = (double) suspiciousClicks / clicks.size();
        if (suspiciousRatio > 0.5) {
            return 0;
        } else if (suspiciousRatio > 0.2) {
            return 5;
        }
        return 10;
    }
    
    private int analyzeClickPattern(List<Map<String, Object>> clicks) {
        // 分析点击模式随机性
        return 10;
    }
    
    private int analyzeClickPath(TrafficData data) {
        // 分析点击路径
        return 5;
    }
    
    private int analyzeDisplayInteraction(TrafficData data) {
        // 分析展示互动
        return 5;
    }
    
    private int analyzeSessionDuration(Map<String, Object> sessionData) {
        double duration = Double.parseDouble(sessionData.get("duration").toString());
        if (duration < 5) {
            return 0;
        } else if (duration < 30) {
            return 5;
        }
        return 10;
    }
    
    private int analyzeUserInteraction(TrafficData data) {
        // 分析用户互动
        return 10;
    }
    
    private int analyzeConversionBehavior(TrafficData data) {
        // 分析转化行为
        return 5;
    }
    
    private int checkDeviceFingerprint(TrafficData data) {
        // 检查设备指纹
        return 5;
    }
    
    private int analyzeBrowserBehavior(TrafficData data) {
        // 分析浏览器行为
        return 5;
    }
    
    private int analyzeUserAgent(TrafficData data) {
        // 分析User-Agent
        return 5;
    }
} 