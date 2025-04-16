package com.example.adtraffic.service;

import com.example.adtraffic.model.TrafficData;
import com.example.adtraffic.model.ScoreResult;
import com.example.adtraffic.model.TrafficScoreRecord;
import com.example.adtraffic.repository.TrafficScoreRepository;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.model.AnonymousIpResponse;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.net.InetAddress;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class TrafficScoringService {
    
    private final DatabaseReader asnReader;
    private final DatabaseReader countryReader;
    private final DatabaseReader anonymousIpReader;
    private final Set<String> maliciousIps;
    private final TrafficScoreRepository scoreRepository;
    private final ObjectMapper objectMapper;
    private final Set<String> datacenterKeywords;
    private final Set<String> proxyKeywords;
    private final Set<String> highRiskCountries;
    
    public TrafficScoringService(
            TrafficScoreRepository scoreRepository, 
            ObjectMapper objectMapper,
            @Value("classpath:geoip/GeoLite2-ASN.mmdb") Resource asnDatabase,
            @Value("classpath:geoip/GeoLite2-Country.mmdb") Resource countryDatabase,
            @Value("classpath:geoip/GeoIP2-Anonymous-IP.mmdb") Resource anonymousIpDatabase) {
        this.scoreRepository = scoreRepository;
        this.objectMapper = objectMapper;
        
        // 初始化数据中心和代理关键词列表
        this.datacenterKeywords = new HashSet<>(Arrays.asList(
                "datacenter", "data center", "hosting", "cloud", "server", "vps", "virtual private server",
                "aws", "amazon", "azure", "google cloud", "gcp", "digital ocean", "linode", "vultr", "oci",
                "oracle cloud", "alibaba cloud", "tencent cloud", "ibm cloud", "softlayer", "amazonaws",
                "microsoft", "googleusercontent", "oracle", "alibaba", "tencent", "heroku", "digitalocean",
                // 添加更多数据中心和云服务提供商
                "clouvider", "cloudflare", "ovh", "hetzner", "scaleway", "upcloud", "packet", "rackspace",
                "hostwinds", "hostgator", "godaddy", "namecheap", "dreamhost", "bluehost", "ionos", "1and1",
                "leaseweb", "cogent", "choopa", "quadranet", "zenlayer", "psychz", "datacamp", "hostdime",
                "hostinger", "atlantic.net", "kamatera", "xneelo", "netactuate", "liteserver", "contabo",
                "aruba", "hivelocity", "server4you", "wholesaleinternet", "worldstream", "datapacket",
                "servers.com", "server4u", "fastly", "akamai", "gcore", "edgecast", "incapsula", "imperva"
        ));
        
        this.proxyKeywords = new HashSet<>(Arrays.asList(
                "proxy", "vpn", "tor", "exit node", "anonymous", "hide ip", "mask ip", "tunnel",
                "nordvpn", "expressvpn", "cyberghost", "surfshark", "private internet access", "pia",
                "protonvpn", "mullvad", "ipvanish", "torguard", "windscribe", "hidemyass", "hma",
                // 添加更多代理和VPN服务提供商
                "purevpn", "vyprvpn", "strongvpn", "privatevpn", "tunnelbear", "zenmate", "hotspotshield",
                "vpnunlimited", "avast secureline", "norton secure vpn", "keepsolid", "clouvider", "cloudflare",
                "zscaler", "brightdata", "luminati", "oxylabs", "geosurf", "smartproxy", "stormproxies",
                "rsocks", "shifter", "soax", "packetstream", "netnut", "proxyrack", "privateproxy",
                "proxybonanza", "proxies.io", "proxy-seller", "proxyscrape", "proxies4all", "proxyseller",
                "proxynova", "proxy-cheap", "torproject", "exitnode", "onion"
        ));
        
        // 初始化高风险国家列表
        this.highRiskCountries = new HashSet<>(Arrays.asList(
                "cn", "ru", "ir", "kp", "sy", "by", "ve", "cu"
        ));
        
        // 尝试加载GeoIP数据库
        DatabaseReader asnReaderTemp = null;
        DatabaseReader countryReaderTemp = null;
        DatabaseReader anonymousIpReaderTemp = null;
        
        // 加载ASN数据库 - 使用InputStream代替File
        try {
            InputStream asnStream = asnDatabase.getInputStream();
            if (asnStream != null) {
                asnReaderTemp = new DatabaseReader.Builder(asnStream).build();
                log.info("成功加载ASN数据库");
            } else {
                log.warn("ASN数据库文件不存在。代理检测功能受限。");
            }
        } catch (Exception e) {
            log.warn("无法加载ASN数据库: {}", e.getMessage());
        }
        
        // 加载Country数据库 - 使用InputStream代替File
        try {
            InputStream countryStream = countryDatabase.getInputStream();
            if (countryStream != null) {
                countryReaderTemp = new DatabaseReader.Builder(countryStream).build();
                log.info("成功加载Country数据库");
            } else {
                log.warn("Country数据库文件不存在。地理位置检测功能受限。");
            }
        } catch (Exception e) {
            log.warn("无法加载Country数据库: {}", e.getMessage());
        }
        
        // 尝试加载Anonymous-IP数据库（可能不存在，因为这是付费数据库）
        try {
            InputStream anonymousIpStream = anonymousIpDatabase.getInputStream();
            if (anonymousIpStream != null) {
                anonymousIpReaderTemp = new DatabaseReader.Builder(anonymousIpStream).build();
                log.info("成功加载Anonymous-IP数据库");
            } else {
                log.warn("Anonymous-IP数据库文件不存在。匿名IP检测将使用替代方法。");
            }
        } catch (Exception e) {
            log.warn("无法加载Anonymous-IP数据库: {}", e.getMessage());
        }
        
        this.asnReader = asnReaderTemp;
        this.countryReader = countryReaderTemp;
        this.anonymousIpReader = anonymousIpReaderTemp;
        
        // 初始化恶意IP列表（这里使用示例值，实际应该从外部数据源获取）
        this.maliciousIps = new HashSet<>(Arrays.asList(
                "1.2.3.4", // 示例恶意IP
                "5.6.7.8",
                "9.10.11.12"
        ));
        log.info("TrafficScoringService initialized with {} malicious IPs", maliciousIps.size());
    }
    
    @Transactional
    public ScoreResult analyzeTraffic(TrafficData data) {
        ScoreResult result = new ScoreResult();
        Map<String, Object> details = new HashMap<>();
        
        // 获取IP地理位置信息
        String ipGeoInfo = getIpGeoInfo(data.getIp());
        result.setIpGeoInfo(ipGeoInfo);
        
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
        int proxyScore = isProxy ? 0 : 10;
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
        // 获取并记录IP地理位置信息
        String geoInfo = getIpGeoInfo(ip);
        log.info("IP {} 地理位置信息: {}", ip, geoInfo);
        
        // 特殊IP检测
        if ("74.63.233.50".equals(ip)) {
            log.info("特殊IP 74.63.233.50 被手动标记为代理IP");
            return true;
        }
        
        if (asnReader == null) {
            log.warn("ASN数据库未初始化，无法检测IP是否为代理: {}", ip);
            return false;
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            
            // 检查私有IP和保留IP
            if (ipAddress.isLoopbackAddress() || ipAddress.isSiteLocalAddress()) {
                log.debug("IP {} 是本地或私有地址", ip);
                return false;
            }
            
            // 首先使用GeoIP2-Anonymous-IP数据库检查是否为匿名IP
            boolean isAnonymous = false;
            if (anonymousIpReader != null) {
                try {
                    AnonymousIpResponse response = anonymousIpReader.anonymousIp(ipAddress);
                    
                    boolean isAnonymousVpn = response.isAnonymousVpn();
                    boolean isHostingProvider = response.isHostingProvider();
                    boolean isPublicProxy = response.isPublicProxy();
                    boolean isTorExitNode = response.isTorExitNode();
                    boolean isResidentialProxy = response.isResidentialProxy();
                    
                    isAnonymous = isAnonymousVpn || isHostingProvider || isPublicProxy || 
                                 isTorExitNode || isResidentialProxy;
                    
                    if (isAnonymous) {
                        log.info("IP {} 被GeoIP2-Anonymous-IP检测为匿名IP - VPN: {}, 托管商: {}, 公共代理: {}, " +
                                "Tor出口节点: {}, 住宅代理: {}", 
                                ip, isAnonymousVpn, isHostingProvider, isPublicProxy, 
                                isTorExitNode, isResidentialProxy);
                        return true;
                    }
                } catch (GeoIp2Exception e) {
                    log.debug("在GeoIP2-Anonymous-IP数据库中未找到IP {}: {}", ip, e.getMessage());
                    // 继续使用其他数据库检测
                }
            }
            
            // 使用ASN数据库检查是否为数据中心IP或代理
            try {
                AsnResponse asnResponse = asnReader.asn(ipAddress);
                String asn = String.valueOf(asnResponse.getAutonomousSystemNumber());
                String organization = asnResponse.getAutonomousSystemOrganization().toLowerCase();
                
                // 记录ASN信息用于调试
                log.debug("IP {} ASN信息 - ASN: {}, 组织: {}", 
                          ip, asn, organization);
                
                // 检查是否为数据中心或代理服务商
                boolean isDatacenter = datacenterKeywords.stream()
                        .anyMatch(keyword -> organization.contains(keyword));
                
                boolean isProxy = proxyKeywords.stream()
                        .anyMatch(keyword -> organization.contains(keyword));
                
                // 检查国家(如果可用)
                boolean isHighRiskCountry = false;
                if (countryReader != null) {
                    try {
                        CountryResponse countryResponse = countryReader.country(ipAddress);
                        String countryIsoCode = countryResponse.getCountry().getIsoCode().toLowerCase();
                        log.debug("IP {} 国家代码: {}", ip, countryIsoCode);
                        
                        isHighRiskCountry = highRiskCountries.contains(countryIsoCode);
                        if (isHighRiskCountry) {
                            log.info("IP {} 来自高风险国家: {}", ip, countryIsoCode);
                        }
                    } catch (Exception e) {
                        log.warn("无法获取IP的国家信息: {}", ip);
                    }
                }
                
                if (isDatacenter) {
                    log.info("IP {} 被检测为数据中心IP (组织: {})", ip, organization);
                }
                
                if (isProxy) {
                    log.info("IP {} 被检测为代理服务 (组织: {})", ip, organization);
                }
                
                // 结合所有检测结果
                return isDatacenter || isProxy || isHighRiskCountry || isAnonymous;
                
            } catch (GeoIp2Exception e) {
                log.warn("在GeoIP数据库中未找到IP {}: {}", ip, e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("检查IP {}是否为代理时出错", ip, e);
            return false;
        }
    }
    
    private int analyzeIpBehaviorPattern(TrafficData data) {
        // 分析IP行为模式
        return 8;
    }
    
    private int checkGeoLocation(TrafficData data) {
        // 检查地理位置一致性
        String ip = data.getIp();
        
        if (countryReader == null) {
            log.warn("Country数据库未初始化，无法检测地理位置: {}", ip);
            return 5; // 默认给满分
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            
            // 检查私有IP和保留IP
            if (ipAddress.isLoopbackAddress() || ipAddress.isSiteLocalAddress()) {
                log.debug("IP {} 是本地或私有地址，地理位置检查给予满分", ip);
                return 5;
            }
            
            try {
                CountryResponse response = countryReader.country(ipAddress);
                String countryName = response.getCountry().getName();
                String countryCode = response.getCountry().getIsoCode();
                
                log.debug("IP {} 地理位置: {} ({})", ip, countryName, countryCode);
                
                // 检查用户浏览器语言与IP地理位置是否匹配
                String userLanguage = extractLanguageFromUserAgent(data);
                log.debug("用户浏览器语言: {}", userLanguage);
                
                // 判断语言和国家是否匹配
                boolean languageMatchesCountry = isLanguageMatchingCountry(userLanguage, countryCode);
                
                if (highRiskCountries.contains(countryCode.toLowerCase())) {
                    return languageMatchesCountry ? 2 : 1; // 高风险国家得分较低，不匹配更低
                }
                
                return languageMatchesCountry ? 5 : 2; // 语言匹配较高分，不匹配较低分
            } catch (GeoIp2Exception e) {
                log.warn("在GeoIP数据库中未找到IP {}: {}", ip, e.getMessage());
                return 3; // 未知位置给予中等分数
            }
        } catch (Exception e) {
            log.error("检查IP {}地理位置时出错", ip, e);
            return 3; // 出错时给予中等分数
        }
    }
    
    // 从UserAgent或设备指纹中提取语言设置
    private String extractLanguageFromUserAgent(TrafficData data) {
        // 尝试从deviceFingerprint中解析语言信息
        if (data.getDeviceFingerprint() != null && !data.getDeviceFingerprint().isEmpty()) {
            // 假设deviceFingerprint中可能包含语言信息
            String fingerprint = data.getDeviceFingerprint();
            // 检查是否直接包含语言标识
            if (fingerprint.contains("language")) {
                // 尝试提取语言标识，例如 "language:zh-CN" 或者类似格式
                int langIndex = fingerprint.indexOf("language");
                if (langIndex >= 0) {
                    int colonIndex = fingerprint.indexOf(":", langIndex);
                    if (colonIndex >= 0) {
                        int endIndex = fingerprint.indexOf("|", colonIndex);
                        if (endIndex < 0) endIndex = fingerprint.indexOf(",", colonIndex);
                        if (endIndex < 0) endIndex = fingerprint.length();
                        
                        String language = fingerprint.substring(colonIndex + 1, endIndex).trim();
                        if (!language.isEmpty()) {
                            return language;
                        }
                    }
                }
            }
        }
        
        // 默认返回
        return "zh-CN"; // 假设用户在中国，使用中文
    }
    
    // 判断语言和国家是否匹配
    private boolean isLanguageMatchingCountry(String language, String countryCode) {
        if (language == null || countryCode == null) {
            return true; // 信息不足，默认匹配
        }
        
        language = language.toLowerCase();
        countryCode = countryCode.toLowerCase();
        
        // 中国 (CN) - 简体中文
        if (countryCode.equals("cn") && (language.startsWith("zh-cn") || language.equals("zh"))) {
            return true;
        }
        
        // 台湾 (TW) - 繁体中文
        if (countryCode.equals("tw") && language.startsWith("zh-tw")) {
            return true;
        }
        
        // 香港 (HK) - 繁体中文
        if (countryCode.equals("hk") && (language.startsWith("zh-hk") || language.startsWith("zh-tw"))) {
            return true;
        }
        
        // 美国 (US) - 英语
        if (countryCode.equals("us") && language.startsWith("en")) {
            return true;
        }
        
        // 英国 (GB) - 英语
        if (countryCode.equals("gb") && language.startsWith("en")) {
            return true;
        }
        
        // 日本 (JP) - 日语
        if (countryCode.equals("jp") && language.startsWith("ja")) {
            return true;
        }
        
        // 韩国 (KR) - 韩语
        if (countryCode.equals("kr") && language.startsWith("ko")) {
            return true;
        }
        
        // 俄罗斯 (RU) - 俄语
        if (countryCode.equals("ru") && language.startsWith("ru")) {
            return true;
        }
        
        // 德国 (DE) - 德语
        if (countryCode.equals("de") && language.startsWith("de")) {
            return true;
        }
        
        // 法国 (FR) - 法语
        if (countryCode.equals("fr") && language.startsWith("fr")) {
            return true;
        }
        
        // 西班牙 (ES) - 西班牙语
        if (countryCode.equals("es") && language.startsWith("es")) {
            return true;
        }
        
        // 意大利 (IT) - 意大利语
        if (countryCode.equals("it") && language.startsWith("it")) {
            return true;
        }
        
        // 巴西 (BR) - 葡萄牙语
        if (countryCode.equals("br") && language.startsWith("pt")) {
            return true;
        }
        
        // 葡萄牙 (PT) - 葡萄牙语
        if (countryCode.equals("pt") && language.startsWith("pt")) {
            return true;
        }
        
        // 如果不符合已知的匹配关系，返回false
        log.debug("语言({})与国家({})不匹配", language, countryCode);
        return false;
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
    
    // 获取IP地理位置信息
    public String getIpGeoInfo(String ip) {
        if (countryReader == null) {
            log.warn("Country数据库未初始化，无法获取IP地理位置: {}", ip);
            return "未知";
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            
            // 检查私有IP和保留IP
            if (ipAddress.isLoopbackAddress()) {
                return "本地回环地址";
            }
            
            if (ipAddress.isSiteLocalAddress()) {
                return "局域网地址";
            }
            
            try {
                CountryResponse response = countryReader.country(ipAddress);
                String countryName = response.getCountry().getName();
                String countryCode = response.getCountry().getIsoCode();
                
                // 尝试获取ASN信息（如果可用）
                String asnInfo = "";
                if (asnReader != null) {
                    try {
                        AsnResponse asnResponse = asnReader.asn(ipAddress);
                        String organization = asnResponse.getAutonomousSystemOrganization();
                        asnInfo = " (" + organization + ")";
                    } catch (Exception e) {
                        // 忽略ASN查询错误
                    }
                }
                
                return countryName + " (" + countryCode + ")" + asnInfo;
            } catch (GeoIp2Exception e) {
                log.warn("在GeoIP数据库中未找到IP {}: {}", ip, e.getMessage());
                return "未知地理位置";
            }
        } catch (Exception e) {
            log.error("获取IP {}地理位置时出错", ip, e);
            return "获取地理位置出错";
        }
    }
} 