package com.example.adtraffic.model;

import lombok.Data;
import java.util.Map;

@Data
public class ScoreResult {
    private int totalScore;
    private Map<String, Object> details;
    private String conclusion;
    private String ipGeoInfo;
    
    public void setTotalScore(int score) {
        this.totalScore = score;
        if (score >= 80) {
            this.conclusion = "真实流量";
        } else if (score >= 50) {
            this.conclusion = "可疑流量";
        } else {
            this.conclusion = "假流量";
        }
    }
} 