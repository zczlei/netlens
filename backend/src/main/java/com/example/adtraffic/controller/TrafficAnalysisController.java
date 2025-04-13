package com.example.adtraffic.controller;

import com.example.adtraffic.model.TrafficData;
import com.example.adtraffic.model.ScoreResult;
import com.example.adtraffic.service.TrafficScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class TrafficAnalysisController {

    private final TrafficScoringService scoringService;

    @Autowired
    public TrafficAnalysisController(TrafficScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @PostMapping("/traffic-analysis")
    public ScoreResult analyzeTraffic(@RequestBody TrafficData data) {
        log.info("Received traffic analysis request for IP: {}", data.getIp());
        return scoringService.analyzeTraffic(data);
    }
} 