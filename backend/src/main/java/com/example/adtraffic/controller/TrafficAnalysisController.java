package com.example.adtraffic.controller;

import com.example.adtraffic.model.TrafficData;
import com.example.adtraffic.model.ScoreResult;
import com.example.adtraffic.model.TrafficScoreRecord;
import com.example.adtraffic.service.TrafficScoringService;
import com.example.adtraffic.repository.TrafficScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class TrafficAnalysisController {

    private final TrafficScoringService trafficScoringService;
    private final TrafficScoreRepository trafficScoreRepository;

    @Autowired
    public TrafficAnalysisController(TrafficScoringService trafficScoringService, 
                                    TrafficScoreRepository trafficScoreRepository) {
        this.trafficScoringService = trafficScoringService;
        this.trafficScoreRepository = trafficScoreRepository;
    }

    @PostMapping("/traffic-analysis")
    public ResponseEntity<ScoreResult> analyzeTraffic(@RequestBody TrafficData trafficData,
                                                     @RequestHeader(value = "X-Real-IP", required = false) String realIp) {
        log.info("Received traffic analysis request for IP: {}", trafficData.getIp());
        
        // 如果通过代理，使用X-Real-IP
        if (realIp != null && !realIp.isEmpty() && (trafficData.getIp() == null || trafficData.getIp().isEmpty())) {
            trafficData.setIp(realIp);
        }
        
        ScoreResult result = trafficScoringService.analyzeTraffic(trafficData);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/traffic-records")
    public ResponseEntity<?> getTrafficRecords(
            @RequestParam(required = false) String conclusion,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        
        List<TrafficScoreRecord> records;
        
        if (conclusion != null && !conclusion.isEmpty()) {
            records = trafficScoreRepository.findByConclusion(conclusion);
        } else {
            records = trafficScoreRepository.findTop100ByOrderByCreatedAtDesc();
            if (records.size() > limit) {
                records = records.subList(0, limit);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", records.size());
        
        // 添加统计数据
        List<Object[]> stats = trafficScoreRepository.countByConclusion();
        Map<String, Long> conclusionStats = new HashMap<>();
        
        for (Object[] stat : stats) {
            conclusionStats.put((String) stat[0], (Long) stat[1]);
        }
        
        response.put("statistics", conclusionStats);
        
        return ResponseEntity.ok(response);
    }
} 