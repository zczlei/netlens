package com.example.adtraffic.controller;

import com.example.adtraffic.model.TrafficData;
import com.example.adtraffic.service.TrafficDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/traffic")
@CrossOrigin
@Slf4j
public class TrafficDataController {

    private final TrafficDataService trafficDataService;

    @Autowired
    public TrafficDataController(TrafficDataService trafficDataService) {
        this.trafficDataService = trafficDataService;
    }

    @PostMapping
    public ResponseEntity<?> collectTrafficData(@RequestBody TrafficData data) {
        log.info("Received traffic data collection request for IP: {}", data.getIp());
        TrafficData savedData = trafficDataService.saveTrafficData(data);
        return ResponseEntity.ok().body(savedData);
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestTrafficData() {
        TrafficData latestData = trafficDataService.getLatestTrafficData();
        if (latestData != null) {
            log.info("Returning latest traffic data for IP: {}", latestData.getIp());
            return ResponseEntity.ok().body(latestData);
        } else {
            log.info("No traffic data found");
            return ResponseEntity.notFound().build();
        }
    }
} 