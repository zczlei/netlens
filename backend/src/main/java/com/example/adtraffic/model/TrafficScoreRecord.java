package com.example.adtraffic.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "traffic_score_records")
public class TrafficScoreRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ip;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "ip_score")
    private Integer ipScore;

    @Column(name = "click_score")
    private Integer clickScore;

    @Column(name = "session_score")
    private Integer sessionScore;

    @Column(name = "device_score")
    private Integer deviceScore;

    @Column(length = 20)
    private String conclusion;

    @Column(name = "score_details", columnDefinition = "TEXT")
    private String scoreDetails;  // JSON格式存储详细评分数据

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 