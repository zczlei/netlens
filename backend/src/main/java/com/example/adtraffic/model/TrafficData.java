package com.example.adtraffic.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TrafficData {
    private String ip;
    private String userAgent;
    private long startTime;
    private List<Map<String, Object>> clicks;
    private List<Map<String, Object>> mouseMovements;
    private List<Map<String, Object>> scrollEvents;
    private Map<String, Object> sessionData;
    private String deviceFingerprint;
} 