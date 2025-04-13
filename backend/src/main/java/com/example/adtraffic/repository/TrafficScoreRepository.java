package com.example.adtraffic.repository;

import com.example.adtraffic.model.TrafficScoreRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrafficScoreRepository extends JpaRepository<TrafficScoreRecord, Long> {
    
    // 根据IP查询记录
    List<TrafficScoreRecord> findByIp(String ip);
    
    // 查询某个时间段内的记录
    List<TrafficScoreRecord> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // 查询某个分数范围内的记录
    List<TrafficScoreRecord> findByTotalScoreBetween(int minScore, int maxScore);
    
    // 统计不同结论的数量
    @Query("SELECT t.conclusion, COUNT(t) FROM TrafficScoreRecord t GROUP BY t.conclusion")
    List<Object[]> countByConclusion();
    
    // 查询最近的可疑流量
    @Query("SELECT t FROM TrafficScoreRecord t WHERE t.conclusion = '可疑流量' ORDER BY t.createdAt DESC")
    List<TrafficScoreRecord> findRecentSuspiciousTraffic();
} 