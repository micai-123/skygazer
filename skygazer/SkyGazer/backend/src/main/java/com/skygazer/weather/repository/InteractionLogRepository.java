package com.skygazer.weather.repository;

import com.skygazer.weather.entity.InteractionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionLogRepository extends JpaRepository<InteractionLog, Long> {
    
    List<InteractionLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<InteractionLog> findByInteractionType(String interactionType);
    
    long countByUserId(Long userId);
}
