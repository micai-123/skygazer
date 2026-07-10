package com.skygazer.weather.repository;

import com.skygazer.weather.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    
    Optional<WeatherData> findFirstByLocationOrderByRecordTimeDesc(String location);
    
    List<WeatherData> findByLocationAndRecordTimeBetweenOrderByRecordTimeAsc(
        String location, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT w FROM WeatherData w WHERE w.location = :location " +
           "AND w.recordTime >= :startTime ORDER BY w.recordTime DESC")
    List<WeatherData> findRecentByLocation(@Param("location") String location, 
                                           @Param("startTime") LocalDateTime startTime);
    
    void deleteByRecordTimeBefore(LocalDateTime time);
}
